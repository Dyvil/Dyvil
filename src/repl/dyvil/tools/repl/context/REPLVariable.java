package dyvil.tools.repl.context;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.repl.DyvilREPL;

public class REPLVariable extends Field
{
	private REPLContext context;

	protected String bytecodeName;
	protected String className;
	private   Class  theClass;

	public REPLVariable(REPLContext context, ICodePosition position, Name name, IType type, IValue value, String className, ModifierSet modifiers)
	{
		super(null, name, type);
		this.context = context;
		this.className = className;
		this.modifiers = modifiers;
		this.position = position;
		this.value = value;

		REPLContext.updateModifiers(modifiers);
	}

	@Override
	public boolean hasModifier(int mod)
	{
		return mod == Modifiers.STATIC || this.modifiers.hasIntModifier(mod);
	}

	private static void filterStackTrace(Throwable throwable)
	{
		StackTraceElement[] traceElements = throwable.getStackTrace();
		int count = traceElements.length;
		int lastIndex = count - 1;

		for (; lastIndex >= 0; --lastIndex)
		{
			if (traceElements[lastIndex].getClassName().startsWith("sun.misc.Unsafe"))
			{
				--lastIndex;
				break;
			}
		}

		StackTraceElement[] newTraceElements = new StackTraceElement[lastIndex + 1];
		System.arraycopy(traceElements, 0, newTraceElements, 0, lastIndex + 1);

		throwable.setStackTrace(newTraceElements);

		Throwable cause = throwable.getCause();
		if (cause != null)
		{
			filterStackTrace(cause);
		}

		for (Throwable suppressed : throwable.getSuppressed())
		{
			filterStackTrace(suppressed);
		}
	}

	protected void compute(DyvilREPL repl, List<IClassCompilable> compilableList)
	{
		if (this.isConstant() && !compilableList.isEmpty())
		{
			return;
		}

		try
		{
			this.theClass = this.generateClass(this.className, compilableList);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace(repl.getErrorOutput());
		}

		try
		{
			this.updateValue(repl);
		}
		catch (Throwable t)
		{
			filterStackTrace(t);
			t.printStackTrace(repl.getOutput());
		}
	}

	protected void updateValue(DyvilREPL repl)
	{
		if (this.theClass == null)
		{
			return;
		}

		try
		{
			if (this.type == Types.VOID)
			{
				ReflectUtils.UNSAFE.ensureClassInitialized(this.theClass);
			}
			else
			{
				java.lang.reflect.Field field = this.theClass.getDeclaredFields()[0];
				field.setAccessible(true);
				Object result = field.get(null);
				this.value = new REPLResult(result);
			}
		}
		catch (IllegalAccessException illegalAccess)
		{
			illegalAccess.printStackTrace(repl.getOutput());
		}
		catch (ExceptionInInitializerError initializerError)
		{
			final Throwable cause = initializerError.getCause();
			filterStackTrace(cause);
			cause.printStackTrace(repl.getOutput());
		}
		catch (Throwable throwable)
		{
			filterStackTrace(throwable);
			throwable.printStackTrace(repl.getOutput());
		}
	}

	private boolean isConstant()
	{
		return this.hasModifier(Modifiers.FINAL) && this.value != null && isConstant(this.value);
	}

	private static boolean isConstant(IValue value)
	{
		int tag = value.valueTag();
		return tag >= 0 && tag != IValue.NIL && tag < IValue.STRING;
	}

	private Class generateClass(String className, List<IClassCompilable> compilableList) throws Throwable
	{
		final String name = this.bytecodeName = this.name.qualified;
		final String extendedType = this.type.getExtendedName();
		final String methodType = "()" + extendedType;

		final ClassWriter classWriter = new ClassWriter();
		// Generate Class Header
		classWriter
			.visit(ClassFormat.CLASS_VERSION, Modifiers.PUBLIC | Modifiers.FINAL | ClassFormat.ACC_SUPER, className,
			       null, "java/lang/Object", null);

		classWriter.visitSource(className, null);

		if (this.type != Types.VOID)
		{
			// Generate the field holding the value
			classWriter.visitField(this.modifiers.toFlags(), name, extendedType, null, null);
		}

		// Compilables
		for (IClassCompilable compilable : compilableList)
		{
			compilable.write(classWriter);
		}

		// Generate <clinit> static initializer
		final MethodWriter clinitWriter = new MethodWriterImpl(classWriter, classWriter.visitMethod(
			Modifiers.STATIC | Modifiers.SYNTHETIC, "<clinit>", "()V", null, null));
		clinitWriter.visitCode();

		for (IClassCompilable c : compilableList)
		{
			c.writeStaticInit(clinitWriter);
		}

		// Write a call to the computeResult method
		clinitWriter.visitMethodInsn(Opcodes.INVOKESTATIC, className, "computeResult", methodType, false);
		if (this.type != Types.VOID)
		{
			// Store the value to the field
			clinitWriter.visitFieldInsn(Opcodes.PUTSTATIC, className, name, extendedType);
		}

		// Finish the <clinit> static initializer
		clinitWriter.visitInsn(Opcodes.RETURN);
		clinitWriter.visitEnd();

		// Writer the computeResult method
		if (this.value != null)
		{
			final MethodWriter computeWriter = new MethodWriterImpl(classWriter, classWriter.visitMethod(
				Modifiers.PRIVATE | Modifiers.STATIC, "computeResult", methodType, null, null));
			computeWriter.visitCode();
			this.value.writeExpression(computeWriter, this.type);
			computeWriter.visitEnd(this.type);
		}

		// Finish Class compilation
		classWriter.visitEnd();

		final byte[] bytes = classWriter.toByteArray();

		if (this.type != Types.VOID || !compilableList.isEmpty())
		{
			// The type contains the value, so we have to keep the class loaded.
			return REPLMemberClass.loadClass(this.context.repl, className, bytes);
		}

		// We don't have any variables, so we can throw the Class away after
		// it has been loaded.
		return REPLMemberClass.loadAnonymousClass(this.context.repl, className, bytes);
	}

	@Override
	public void writeGet(MethodWriter writer, IValue receiver, int lineNumber) throws BytecodeException
	{
		if (this.isConstant())
		{
			this.value.writeExpression(writer, this.type);
			return;
		}

		if (this.className == null)
		{
			this.type.writeDefaultValue(writer);
			return;
		}

		String extended = this.type.getExtendedName();
		writer.visitFieldInsn(Opcodes.GETSTATIC, this.className, this.bytecodeName, extended);
	}

	@Override
	public void writeSet(MethodWriter writer, IValue receiver, IValue value, int lineNumber) throws BytecodeException
	{
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}

		if (this.className == null)
		{
			writer.visitInsn(Opcodes.AUTO_POP);
			return;
		}

		String extended = this.type.getExtendedName();
		writer.visitFieldInsn(Opcodes.PUTSTATIC, this.className, this.bytecodeName, extended);
	}
}

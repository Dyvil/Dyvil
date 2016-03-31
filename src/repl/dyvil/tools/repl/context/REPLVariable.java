package dyvil.tools.repl.context;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;
import dyvil.tools.repl.DyvilREPL;

import java.lang.reflect.InvocationTargetException;

public class REPLVariable extends Field
{
	private REPLContext context;

	protected String   bytecodeName;
	protected String   className;
	private   Class<?> theClass;
	private   Object   displayValue;

	public REPLVariable(REPLContext context, ICodePosition position, Name name, IType type, String className, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
		this.context = context;
		this.className = className;

		REPLContext.updateModifiers(modifiers);
	}

	@Override
	public boolean hasModifier(int mod)
	{
		return mod == Modifiers.STATIC || this.modifiers.hasIntModifier(mod);
	}

	protected void compute(DyvilREPL repl, List<IClassCompilable> compilableList)
	{
		if (this.isConstant() && !compilableList.isEmpty())
		{
			return;
		}

		final byte[] bytes;

		try
		{
			bytes = this.generateClass(this.className, compilableList);
		}
		catch (Throwable throwable)
		{
			throwable.printStackTrace(repl.getErrorOutput());
			return;
		}

		if (this.type == Types.VOID && compilableList.isEmpty())
		{
			// We don't have any variables, so the Class can be GC'd after it has been loaded and initialized.
			this.theClass = REPLCompiler.loadAnonymousClass(this.context.repl, this.className, bytes);
			return;
		}

		// The type contains the value, so we have to keep the class loaded.
		this.theClass = REPLCompiler.loadClass(this.context.repl, this.className, bytes);

		this.updateValue(repl);
	}

	protected void updateValue(DyvilREPL repl)
	{
		if (this.theClass == null || this.type == Types.VOID)
		{
			return;
		}

		final Object result;
		if (this.property != null)
		{
			try
			{
				final java.lang.reflect.Method method = this.theClass.getDeclaredMethod(this.name.qualified);
				method.setAccessible(true);
				result = method.invoke(null);
			}
			catch (InvocationTargetException ex)
			{
				ex.getCause().printStackTrace(repl.getOutput());
				this.displayValue = "<error>";
				this.value = null;
				return;
			}
			catch (ReflectiveOperationException ex)
			{
				ex.printStackTrace(repl.getErrorOutput());
				this.displayValue = "<error>";
				this.value = null;
				return;
			}
		}
		else
		{
			try
			{
				final java.lang.reflect.Field field = this.theClass.getDeclaredFields()[0];
				field.setAccessible(true);
				result = field.get(null);
			}
			catch (ReflectiveOperationException ex)
			{
				ex.printStackTrace(repl.getErrorOutput());
				this.displayValue = "<error>";
				this.value = null;
				return;
			}
		}

		this.value = IValue.fromObject(result);
		this.displayValue = result;
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

	private byte[] generateClass(String className, List<IClassCompilable> compilableList) throws Throwable
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

		if (this.property != null)
		{
			this.property.writeStaticInit(clinitWriter);
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

		// Write the property, if necessary
		if (this.property != null)
		{
			this.property.write(classWriter);
		}

		// Finish Class compilation
		classWriter.visitEnd();

		return classWriter.toByteArray();
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

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.annotations != null)
		{
			this.annotations.toString(prefix, buffer);
		}
		this.modifiers.toString(buffer);

		IDataMember.toString(prefix, buffer, this, "field.type_ascription");

		if (this.displayValue != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');

			buffer.append(this.displayValue);
		}
	}
}

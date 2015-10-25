package dyvil.tools.repl;

import dyvil.collection.List;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.*;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class REPLVariable extends Field
{
	protected String	className;
	private Class		theClass;
	
	public REPLVariable(ICodePosition position, Name name, IType type, IValue value, String className, int modifiers)
	{
		super(null, name, type);
		this.className = className;
		this.modifiers = modifiers;
		this.position = position;
		this.value = value;
	}
	
	@Override
	public boolean hasModifier(int mod)
	{
		if (mod == Modifiers.STATIC)
		{
			return true;
		}
		return (this.modifiers & mod) == mod;
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value.check(markers, context);
		}
	}
	
	private static boolean isConstant(IValue value)
	{
		int tag = value.valueTag();
		return tag >= 0 && tag != IValue.NIL && tag < IValue.STRING;
	}
	
	protected void updateValue()
	{
		if (this.type == Types.VOID)
		{
			ReflectUtils.unsafe.ensureClassInitialized(this.theClass);
			return;
		}
		
		java.lang.reflect.Field[] fields = this.theClass.getDeclaredFields();
		
		try
		{
			Object result = fields[0].get(null);
			this.value = new REPLResult(result);
		}
		catch (IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
	}
	
	protected void compute()
	{
		List<IClassCompilable> compilableList = REPLContext.compilableList;
		
		if (this.isConstant() && !compilableList.isEmpty())
		{
			return;
		}
		
		try
		{
			this.theClass = this.generateClass(this.className, compilableList);
			this.updateValue();
		}
		catch (ExceptionInInitializerError t)
		{
			Throwable ex = t.getCause();
			System.err.println(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
			StackTraceElement[] trace = ex.getStackTrace();
			int len = trace.length - 10;
			for (int i = 0; i < len; i++)
			{
				System.err.println("\tat " + trace[i]);
			}
			this.value = this.type.getDefaultValue();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	private boolean isConstant()
	{
		return (this.modifiers & Modifiers.FINAL) != 0 && this.value != null && isConstant(this.value);
	}
	
	private Class generateClass(String className, List<IClassCompilable> compilableList) throws Throwable
	{
		String name = this.name.qualified;
		String extendedType = this.type.getExtendedName();
		ClassWriter cw = new ClassWriter();
		// Generate Class Header
		cw.visit(DyvilCompiler.classVersion, Modifiers.PUBLIC | Modifiers.FINAL | ClassFormat.ACC_SUPER, className, null, "java/lang/Object", null);
		
		cw.visitSource(className, null);
		
		if (this.type != Types.VOID)
		{
			// Generate the field holding the value
			cw.visitField(this.modifiers | Modifiers.PUBLIC | Modifiers.STATIC | Modifiers.SYNTHETIC, name, extendedType, null, null);
		}
		
		// Compilables
		for (IClassCompilable c : compilableList)
		{
			c.write(cw);
		}
		
		// Generate <clinit> static initializer
		MethodWriter mw = new MethodWriterImpl(cw, cw.visitMethod(Modifiers.STATIC | Modifiers.SYNTHETIC, "<clinit>", "()V", null, null));
		mw.begin();
		
		for (IClassCompilable c : compilableList)
		{
			c.writeStaticInit(mw);
		}
		
		// Write the value
		
		if (this.value != null)
		{
			this.writeValue(className, name, extendedType, cw, mw);
		}
		
		// Finish Method compilation
		mw.writeInsn(Opcodes.RETURN);
		mw.end();
		
		// Finish Class compilation
		cw.visitEnd();
		
		byte[] bytes = cw.toByteArray();
		
		if (this.type != Types.VOID || !compilableList.isEmpty())
		{
			// The type contains the value, so we have to keep the class loaded.
			return REPLMemberClass.loadClass(className, bytes);
		}
		// We don't have any variables, so we can throw the Class away after
		// it has been loaded.
		return REPLMemberClass.loadAnonymousClass(className, bytes);
	}
	
	private void writeValue(String className, String name, String extendedType, ClassWriter cw, MethodWriter mw) throws BytecodeException
	{
		if (this.type == Types.VOID)
		{
			this.value.writeStatement(mw);
			return;
		}
		
		String methodType = "()" + extendedType;
		MethodWriter initWriter = new MethodWriterImpl(cw, cw.visitMethod(Modifiers.PRIVATE | Modifiers.STATIC, "computeValue", methodType, null, null));
		initWriter.begin();
		this.value.writeExpression(initWriter, this.type);
		initWriter.end(this.type);
		
		mw.writeInvokeInsn(Opcodes.INVOKESTATIC, className, "computeValue", methodType, false);
		// Store the value to the field
		mw.writeFieldInsn(Opcodes.PUTSTATIC, className, name, extendedType);
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		return null;
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
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
		writer.writeFieldInsn(Opcodes.GETSTATIC, this.className, this.name.qualified, extended);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException
	{
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}
		
		if (this.className == null)
		{
			writer.writeInsn(Opcodes.AUTO_POP);
			return;
		}
		
		String extended = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.PUTSTATIC, this.className, this.name.qualified, extended);
	}
}

package dyvil.tools.repl;

import java.security.ProtectionDomain;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class REPLVariable extends Field
{
	private static final ClassLoader		CLASS_LOADER		= REPLVariable.class.getClassLoader();
	private static final ProtectionDomain	PROTECTION_DOMAIN	= REPLVariable.class.getProtectionDomain();
	
	private static int						classID				= 0;
	
	protected String						className;
	
	public REPLVariable(ICodePosition position, Name name, IType type, IValue value)
	{
		super(null, name, type);
		this.position = position;
		this.value = value;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
			
			if (this.type == Types.UNKNOWN)
			{
				this.type = this.value.getType();
				return;
			}
			
			IValue value1 = this.value.withType(this.type);
			if (value1 == null)
			{
				Marker marker = markers.create(this.value.getPosition(), "field.type", this.name.unqualified);
				marker.addInfo("Field Type: " + this.type);
				marker.addInfo("Value Type: " + this.value.getType());
			}
			else
			{
				this.value = value1;
			}
		}
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
		return tag >= 0 && tag != IValue.NIL && tag <= IValue.STRING;
	}
	
	protected void compute()
	{
		if (this.className != null || isConstant(this.value))
			return;
		
		try
		{
			this.className = "REPL$" + classID++;
			Class c = generateClass(this.value, this.type, this.className);
			
			if (this.type != Types.VOID)
			{
				java.lang.reflect.Field[] fields = c.getDeclaredFields();
				Object result = fields[0].get(null);
				IValue v = IValue.fromObject(result);
				if (v != null)
				{
					this.value = v;
				}
				else
				{
					this.value = new REPLResult(result);
				}
			}
			else
			{
				ReflectUtils.unsafe.ensureClassInitialized(c);
			}
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
	
	private static Class generateClass(IValue value, IType type, String className) throws Throwable
	{
		String extendedType = type.getExtendedName();
		ClassWriter writer = new ClassWriter();
		// Generate Class Header
		writer.visit(DyvilCompiler.classVersion, Modifiers.PUBLIC | Modifiers.FINAL | ClassFormat.ACC_SUPER, className, null, "java/lang/Object", null);
		
		if (type != Types.VOID)
		{
			// Generate the field holding the value
			writer.visitField(Modifiers.PUBLIC | Modifiers.FINAL | Modifiers.STATIC | Modifiers.SYNTHETIC, "value", extendedType, null, null);
		}
		
		// Generate <clinit> static initializer
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.STATIC | Modifiers.SYNTHETIC, "<clinit>", "()V", null, null));
		mw.begin();
		
		// Write the value
		
		if (type != Types.VOID)
		{
			value.writeExpression(mw);
			// Store the value to the field
			mw.writeFieldInsn(Opcodes.PUTSTATIC, className, "value", extendedType);
		}
		else
		{
			value.writeStatement(mw);
		}
		
		// Finish Method compilation
		mw.writeInsn(Opcodes.RETURN);
		mw.end();
		// Finish Class compilation
		writer.visitEnd();
		
		byte[] bytes = writer.toByteArray();
		
		if (type != Types.VOID)
		{
			// The type contains the value, so we have to keep the class loaded.
			return ReflectUtils.unsafe.defineClass(className, bytes, 0, bytes.length, CLASS_LOADER, PROTECTION_DOMAIN);
		}
		// We don't have any variables, so we can throw the Class away after
		// it has been loaded.
		return ReflectUtils.unsafe.defineAnonymousClass(REPLVariable.class, bytes, null);
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		return null;
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		if (isConstant(this.value))
		{
			this.value.writeExpression(writer);
			return;
		}
		
		if (this.className == null)
		{
			this.type.writeDefaultValue(writer);
			return;
		}
		
		String extended = this.type.getExtendedName();
		writer.writeFieldInsn(Opcodes.GETSTATIC, this.className, "value", extended);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
	{
	}
}

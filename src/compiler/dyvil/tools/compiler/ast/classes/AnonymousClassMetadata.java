package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.CaptureField;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class AnonymousClassMetadata implements IClassMetadata
{
	private NestedClass		theClass;
	private IConstructor	constructor;
	private String			desc;
	
	public AnonymousClassMetadata(NestedClass theClass, IConstructor constructor)
	{
		this.theClass = theClass;
		this.constructor = constructor;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
	}
	
	public void writeConstructorCall(MethodWriter writer, IArguments arguments)
	{
		String owner = this.theClass.getInternalName();
		String name = "<init>";
		writer.writeTypeInsn(Opcodes.NEW, owner);
		writer.writeInsn(Opcodes.DUP);
		
		this.constructor.writeArguments(writer, arguments);
		
		int params = this.constructor.parameterCount();
		StringBuilder buf = new StringBuilder();
		buf.append('(');
		for (int i = 0; i < params; i++)
		{
			this.constructor.getParameter(i).getType().appendExtendedName(buf);
		}
		
		CaptureField[] capturedFields = this.theClass.capturedFields;
		int len = this.theClass.capturedFieldCount;
		for (int i = 0; i < len; i++)
		{
			CaptureField field = capturedFields[i];
			field.field.writeGet(writer, null);
			field.getType().appendExtendedName(buf);
		}
		buf.append(")V");
		this.desc = buf.toString();
		
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, name, this.desc, false);
	}
	
	@Override
	public void write(ClassWriter writer, IValue instanceFields)
	{
		CaptureField[] capturedFields = this.theClass.capturedFields;
		int len = this.theClass.capturedFieldCount;
		
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(Modifiers.PROTECTED | Modifiers.MANDATED, "<init>", this.desc, null, null));
		int params = this.constructor.parameterCount();
		for (int i = 0; i < params; i++)
		{
			this.constructor.getParameter(i).write(mw);
		}
		
		int[] indexes = null;
		if (len > 0)
		{
			int index = 0;
			indexes = new int[len];
			
			for (int i = 0; i < len; i++)
			{
				CaptureField field = capturedFields[i];
				field.write(writer);
				indexes[i] = index = mw.registerParameter(index, field.name, field.getType(), 0);
			}
		}
		
		mw.begin();
		mw.writeVarInsn(Opcodes.ALOAD, 0);
		for (int i = 0; i < params; i++)
		{
			IParameter param = this.constructor.getParameter(i);
			mw.writeVarInsn(param.getType().getLoadOpcode(), param.getIndex());
		}
		this.constructor.writeInvoke(mw);
		
		if (len > 0)
		{
			for (int i = 0; i < len; i++)
			{
				CaptureField field = capturedFields[i];
				mw.writeVarInsn(Opcodes.ALOAD, 0);
				mw.writeVarInsn(field.getType().getLoadOpcode(), indexes[i]);
				mw.writeFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), field.name, field.getDescription());
			}
		}
		
		mw.end(Types.VOID);
	}
}

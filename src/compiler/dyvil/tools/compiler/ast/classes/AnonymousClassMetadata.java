package dyvil.tools.compiler.ast.classes;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.field.FieldThis;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.IParameter;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.CaptureHelper;

public class AnonymousClassMetadata implements IClassMetadata
{
	private AnonymousClass theClass;
	private IConstructor   constructor;
	private String         desc;
	
	public AnonymousClassMetadata(AnonymousClass theClass, IConstructor constructor)
	{
		this.theClass = theClass;
		this.constructor = constructor;
	}
	
	private String getDesc()
	{
		if (this.desc != null)
		{
			return this.desc;
		}
		
		int len = this.constructor.parameterCount();
		StringBuilder buf = new StringBuilder();
		
		buf.append('(');
		for (int i = 0; i < len; i++)
		{
			this.constructor.getParameter(i).getType().appendExtendedName(buf);
		}
		
		FieldThis thisField = this.theClass.thisField;
		if (thisField != null)
		{
			buf.append(thisField.getDescription());
		}

		this.theClass.captureHelper.appendCaptureTypes(buf);
		
		return this.desc = buf.append(")V").toString();
	}
	
	public void writeConstructorCall(MethodWriter writer, IArguments arguments) throws BytecodeException
	{
		String owner = this.theClass.getInternalName();
		String name = "<init>";
		writer.writeTypeInsn(Opcodes.NEW, owner);
		writer.writeInsn(Opcodes.DUP);
		
		this.constructor.writeArguments(writer, arguments);
		
		FieldThis thisField = this.theClass.thisField;
		if (thisField != null)
		{
			thisField.getOuter().writeGet(writer);
		}
		
		this.theClass.captureHelper.writeCaptures(writer);
		
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, name, this.getDesc(), false);
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		final CaptureHelper captureHelper = this.theClass.captureHelper;
		captureHelper.writeCaptureFields(writer);

		MethodWriter mw = new MethodWriterImpl(writer,
		                                       writer.visitMethod(Modifiers.MANDATED, "<init>", this.getDesc(), null,
		                                                          null));
		int params = this.constructor.parameterCount();

		// Signature & Parameter Data

		mw.setThisType(this.theClass.getInternalName());

		for (int i = 0; i < params; i++)
		{
			this.constructor.getParameter(i).writeInit(mw);
		}

		int index = mw.localCount();
		int thisIndex = index;

		FieldThis thisField = this.theClass.thisField;
		if (thisField != null)
		{
			thisField.writeField(writer);
			index = mw.registerParameter(index, thisField.getName(), thisField.getTheClass().getType(),
			                             Modifiers.MANDATED);
		}

		captureHelper.writeCaptureParameters(mw, index);

		// Constructor Body
		
		mw.begin();
		mw.writeVarInsn(Opcodes.ALOAD, 0);
		for (int i = 0; i < params; i++)
		{
			IParameter param = this.constructor.getParameter(i);
			param.writeGet(mw);
		}
		this.constructor.writeInvoke(mw, 0);
		
		if (thisField != null)
		{
			mw.writeVarInsn(Opcodes.ALOAD, 0);
			mw.writeVarInsn(Opcodes.ALOAD, thisIndex);
			mw.writeFieldInsn(Opcodes.PUTFIELD, this.theClass.getInternalName(), thisField.getName(),
			                  thisField.getDescription());
		}
		
		captureHelper.writeFieldAssignments(mw);

		this.theClass.writeInit(mw);
		
		mw.end(Types.VOID);
	}
}

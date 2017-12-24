package dyvilx.tools.compiler.ast.field.capture;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.backend.ClassWriter;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.function.Function;

public final class CaptureField extends CaptureDataMember implements IField
{
	public IClass enclosingClass;
	public String internalName;

	public static Function<? super IVariable, ? extends CaptureField> factory(IClass theClass)
	{
		return variable -> new CaptureField(theClass, variable);
	}

	public CaptureField(IClass iclass)
	{
		this.enclosingClass = iclass;
	}

	public CaptureField(IClass iclass, IVariable variable)
	{
		super(variable);
		this.enclosingClass = iclass;

		this.internalName = "this$" + variable.getInternalName();
	}

	@Override
	public void setEnclosingClass(IClass enclosingClass)
	{
		this.enclosingClass = enclosingClass;
	}

	@Override
	public IClass getEnclosingClass()
	{
		return this.enclosingClass;
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.FIELD;
	}

	@Override
	public IValue checkAccess(MarkerList markers, SourcePosition position, IValue receiver, IContext context)
	{
		super.checkAccess(markers, position, receiver, context);

		if (receiver == null)
		{
			return new ThisExpr(this.getPosition(), this.enclosingClass.getThisType(), markers, context);
		}
		return receiver;
	}

	@Override
	public String getInternalName()
	{
		return this.internalName;
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		writer.visitField(Modifiers.MANDATED | Modifiers.SYNTHETIC, this.internalName, this.getDescriptor(),
		                  this.getSignature(), null).visitEnd();
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		String owner = this.enclosingClass.getInternalName();
		String name = this.internalName;
		String desc = this.getDescriptor();
		writer.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.variable.getReferenceType() == null)
		{
			String owner = this.enclosingClass.getInternalName();
			String name = this.internalName;
			String desc = this.variable.getDescriptor();
			writer.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
		}
	}

	@Override
	public void writeSignature(DataOutput out) throws IOException
	{
	}

	@Override
	public void readSignature(DataInput in) throws IOException
	{
	}
}

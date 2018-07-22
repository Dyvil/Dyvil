package dyvilx.tools.compiler.ast.field.capture;

import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.ThisExpr;
import dyvilx.tools.compiler.ast.expression.WriteableExpression;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.Names;
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
		writer.visitField(Modifiers.MANDATED | Modifiers.SYNTHETIC, this.getInternalName(), this.getDescriptor(),
		                  this.getSignature(), null).visitEnd();
	}

	private WriteableExpression asWriteableExpression(WriteableExpression receiver)
	{
		return (writer, type) -> this.writeGetRaw(writer, receiver, 0);
	}

	@Override
	public void writeGetRaw(@NonNull MethodWriter writer, WriteableExpression receiver, int lineNumber)
	{
		receiver.writeExpression(writer, null);

		final String owner = this.getEnclosingClass().getInternalName();
		final String name = this.getInternalName();
		final String desc = this.getDescriptor();
		writer.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
	}

	@Override
	public void writeGet(@NonNull MethodWriter writer, WriteableExpression receiver, int lineNumber)
		throws BytecodeException
	{
		final IType referenceType = this.variable.getReferenceType();
		if (referenceType != null)
		{
			referenceType.resolveField(Names.value).writeGet(writer, this.asWriteableExpression(receiver), lineNumber);
			return;
		}

		this.writeGetRaw(writer, receiver, lineNumber);
	}

	@Override
	public void writeSet(@NonNull MethodWriter writer, WriteableExpression receiver, @NonNull WriteableExpression value,
		int lineNumber) throws BytecodeException
	{
		final IType referenceType = this.variable.getReferenceType();
		assert referenceType != null;
		referenceType.resolveField(Names.value).writeSet(writer, this.asWriteableExpression(receiver), value, lineNumber);
	}

	@Override
	public void writeSetCopy(@NonNull MethodWriter writer, WriteableExpression receiver,
		@NonNull WriteableExpression value, int lineNumber) throws BytecodeException
	{
		final IType referenceType = this.variable.getReferenceType();
		assert referenceType != null;
		referenceType.resolveField(Names.value).writeSetCopy(writer, this.asWriteableExpression(receiver), value, lineNumber);
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

package dyvilx.tools.compiler.ast.field;

import dyvilx.tools.asm.Label;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;

public interface IVariable extends IDataMember
{
	@Override
	default MemberKind getKind()
	{
		return MemberKind.VARIABLE;
	}

	@Override
	default boolean isLocal()
	{
		return true;
	}

	boolean isAssigned();

	default int getLocalSlots()
	{
		return this.getInternalType().getLocalSlots();
	}

	int getLocalIndex();

	void setLocalIndex(int index);

	default boolean isReferenceType()
	{
		return false;
	}

	default void setReferenceType()
	{
	}

	IType getInternalType();

	@Override
	default IDataMember capture(IContext context)
	{
		IDataMember capture = context.capture(this);
		return capture == null ? this : capture;
	}

	@Override
	default IDataMember captureReference(IContext context)
	{
		this.setReferenceType();
		return this.capture(context);
	}

	@Override
	default String getDescriptor()
	{
		return this.getInternalType().getExtendedName();
	}

	@Override
	default String getSignature()
	{
		return this.getInternalType().getSignature();
	}

	default void appendDescription(StringBuilder buf)
	{
		buf.append(this.getDescriptor());
	}

	default void appendSignature(StringBuilder buf)
	{
		buf.append(this.getSignature());
	}

	default void writeLocal(MethodWriter writer, Label start, Label end)
	{
		final IType internalType = this.getInternalType();
		final String signature = internalType.needsSignature() ? internalType.getSignature() : null;

		writer.visitLocalVariable(this.getInternalName(), this.getDescriptor(), signature, start, end,
		                          this.getLocalIndex());
	}

	default void writeInit(MethodWriter writer) throws BytecodeException
	{
		this.writeInit(writer, this.getValue());
	}

	void writeInit(MethodWriter writer, IValue value) throws BytecodeException;
}

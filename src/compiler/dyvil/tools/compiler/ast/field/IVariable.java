package dyvil.tools.compiler.ast.field;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

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

	default boolean isReferenceCapturable()
	{
		return false;
	}

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

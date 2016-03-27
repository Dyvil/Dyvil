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
	default boolean isField()
	{
		return false;
	}
	
	@Override
	default boolean isVariable()
	{
		return true;
	}

	boolean isAssigned();
	
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
	
	default IType getInternalType()
	{
		return this.getType();
	}
	
	@Override
	default IDataMember capture(IContext context)
	{
		IDataMember capture = context.capture(this);
		return capture == null ? this : capture;
	}

	default void appendDescription(StringBuilder buf)
	{
		buf.append(this.getDescriptor());
	}
	
	default void appendSignature(StringBuilder buf)
	{
		buf.append(this.getSignature());
	}

	void writeLocal(MethodWriter writer, Label start, Label end);

	default void writeInit(MethodWriter writer) throws BytecodeException
	{
		this.writeInit(writer, this.getValue());
	}

	void writeInit(MethodWriter writer, IValue value) throws BytecodeException;
}

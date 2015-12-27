package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.type.IType;

public interface IVariable extends IDataMember
{
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
		return this.capture(context, this);
	}
	
	@Override
	default IDataMember capture(IContext context, IVariable variable)
	{
		IDataMember capture = context.capture(this);
		return capture == null ? variable : capture;
	}
	
	default void appendDescription(StringBuilder buf)
	{
		buf.append(this.getDescription());
	}
	
	default void appendSignature(StringBuilder buf)
	{
		buf.append(this.getSignature());
	}
}

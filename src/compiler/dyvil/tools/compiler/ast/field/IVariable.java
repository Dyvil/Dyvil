package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.type.IType;

public interface IVariable extends IDataMember
{
	@Override
	public default boolean isField()
	{
		return false;
	}
	
	@Override
	public default boolean isVariable()
	{
		return true;
	}
	
	public int getLocalIndex();
	
	public void setLocalIndex(int index);
	
	public default boolean isCapturable()
	{
		return false;
	}
	
	public default boolean isReferenceType()
	{
		return false;
	}
	
	public default void setReferenceType()
	{
	}
	
	public default IType getActualType()
	{
		return this.getType();
	}
	
	@Override
	public default IDataMember capture(IContext context)
	{
		return this.capture(context, this);
	}
	
	@Override
	public default IDataMember capture(IContext context, IVariable variable)
	{
		IDataMember capture = context.capture(this);
		return capture == null ? variable : capture;
	}
	
	public default void appendDescription(StringBuilder buf)
	{
		buf.append(this.getDescription());
	}
	
	public default void appendSignature(StringBuilder buf)
	{
		buf.append(this.getSignature());
	}
}

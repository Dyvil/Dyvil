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
	
	public void setIndex(int index);
	
	public int getIndex();
	
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
		IDataMember capture = context.capture(this);
		return capture == null ? this : capture;
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

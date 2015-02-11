package dyvil.tools.compiler.ast.type;


public interface ITyped
{
	public void setType(IType type);
	
	public IType getType();
	
	public default boolean isType(IType type)
	{
		return this.getType() == type;
	}
	
	public default boolean hasType()
	{
		return this.getType() != null;
	}
}

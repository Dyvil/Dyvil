package dyvil.tools.compiler.ast.api;

public interface ITyped
{
	public void setType(IType type);
	
	public IType getType();
	
	public default boolean hasType()
	{
		return this.getType() != null;
	}
}

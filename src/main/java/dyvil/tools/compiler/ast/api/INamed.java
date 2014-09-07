package dyvil.tools.compiler.ast.api;

public interface INamed
{
	public void setName(String name);
	
	public String getName();
	
	public default boolean hasName()
	{
		return this.getName() != null;
	}
}

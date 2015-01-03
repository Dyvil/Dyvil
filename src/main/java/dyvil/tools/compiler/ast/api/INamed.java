package dyvil.tools.compiler.ast.api;

public interface INamed
{
	public void setName(String name);
	
	public String getName();
	
	public void setQualifiedName(String name);
	
	public String getQualifiedName();
	
	public default boolean hasName()
	{
		return this.getName() != null;
	}
	
	public boolean isName(String name);
}

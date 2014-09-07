package dyvil.tools.compiler.ast.api;

public interface IModified
{
	public void setModifiers(int modifiers);
	
	public default void addModifier(int mod)
	{
		this.setModifiers(this.getModifiers() | mod);
	}
	
	public default void removeModifier(int mod)
	{
		this.setModifiers(this.getModifiers() & ~mod);
	}
	
	public int getModifiers();
	
	public default boolean hasModifier(int mod)
	{
		return (this.getModifiers() & mod) == mod;
	}
}

package dyvil.tools.compiler.ast.member;

public interface IModified
{
	public void setModifiers(int modifiers);
	
	public boolean addModifier(int mod);
	
	public void removeModifier(int mod);
	
	public int getModifiers();
	
	public boolean hasModifier(int mod);
}

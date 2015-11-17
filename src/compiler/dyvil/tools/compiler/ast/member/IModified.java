package dyvil.tools.compiler.ast.member;

public interface IModified
{
	void setModifiers(int modifiers);
	
	boolean addModifier(int mod);
	
	void removeModifier(int mod);
	
	int getModifiers();
	
	boolean hasModifier(int mod);
}

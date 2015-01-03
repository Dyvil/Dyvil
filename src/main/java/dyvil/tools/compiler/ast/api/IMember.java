package dyvil.tools.compiler.ast.api;

public interface IMember extends INamed, ITyped, IModified, IAnnotatable
{
	public IClass getTheClass();
	
	public int getAccessLevel();
	
	public byte getAccessibility();
}

package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.classes.IClass;

public interface IMember extends INamed, ITyped, IModified, IAnnotatable
{
	public IClass getTheClass();
	
	public int getAccessLevel();
	
	public byte getAccessibility();
}

package dyvil.tools.compiler.ast.classes;

import dyvil.tools.parsing.Name;

public interface IClassList
{
	public int classCount();
	
	public void addClass(IClass iclass);
	
	public IClass getClass(int index);
	
	public IClass getClass(Name name);
}

package dyvil.tools.compiler.ast.classes;

import dyvil.tools.parsing.Name;

public interface IClassList
{
	int classCount();
	
	void addClass(IClass iclass);
	
	IClass getClass(int index);
	
	IClass getClass(Name name);
}

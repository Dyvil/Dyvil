package dyvil.tools.compiler.ast.header;

import dyvil.tools.compiler.backend.IClassCompilable;

public interface IClassCompilableList
{
	int compilableCount();
	
	void addCompilable(IClassCompilable compilable);
	
	IClassCompilable getCompilable(int index);
}

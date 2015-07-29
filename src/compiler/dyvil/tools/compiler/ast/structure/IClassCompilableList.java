package dyvil.tools.compiler.ast.structure;

import dyvil.tools.compiler.backend.IClassCompilable;

public interface IClassCompilableList
{
	public int compilableCount();
	
	public void addCompilable(IClassCompilable compilable);
	
	public IClassCompilable getCompilable(int index);
}

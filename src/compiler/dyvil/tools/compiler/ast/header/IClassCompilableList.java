package dyvil.tools.compiler.ast.header;

public interface IClassCompilableList
{
	String getInternalName();

	int classCompilableCount();
	
	void addClassCompilable(IClassCompilable compilable);
}

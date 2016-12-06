package dyvil.tools.compiler.ast.header;

public interface ICompilableList
{
	int compilableCount();

	void addCompilable(ICompilable compilable);
}

package dyvilx.tools.compiler.ast.header;

public interface IClassCompilableList
{
	String getInternalName();

	int classCompilableCount();

	void addClassCompilable(ClassCompilable compilable);
}

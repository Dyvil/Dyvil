package dyvil.tools.compiler.ast.header;

public interface IImportList
{
	int importCount();
	
	void addImport(IImport iimport);
	
	void setImport(int index, IImport iimport);
	
	IImport getImport(int index);
}

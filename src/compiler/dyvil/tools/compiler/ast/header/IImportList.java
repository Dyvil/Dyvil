package dyvil.tools.compiler.ast.header;

public interface IImportList
{
	public int importCount();
	
	public void addImport(IImport iimport);
	
	public void setImport(int index, IImport iimport);
	
	public IImport getImport(int index);
}

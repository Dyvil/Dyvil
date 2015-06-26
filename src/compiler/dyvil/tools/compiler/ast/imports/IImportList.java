package dyvil.tools.compiler.ast.imports;

public interface IImportList
{
	public int importCount();
	
	public void addImport(IImport iimport);
	
	public void setImport(int index, IImport iimport);
	
	public IImport getImport(int index);
}

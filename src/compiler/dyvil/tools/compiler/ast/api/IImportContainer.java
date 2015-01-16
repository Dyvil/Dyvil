package dyvil.tools.compiler.ast.api;

public interface IImportContainer extends IASTNode, IContext
{
	public void addImport(IImport iimport);
}

package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.structure.IContext;

public interface IImportContainer extends IASTNode, IContext
{
	public void addImport(IImport iimport);
}

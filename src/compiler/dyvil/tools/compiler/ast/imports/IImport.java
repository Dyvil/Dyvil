package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IImport extends IASTNode, IContext
{
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic);
}

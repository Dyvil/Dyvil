package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.IASTNode;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.marker.Marker;

public interface IImport extends IASTNode, IContext
{
	public void resolveTypes(List<Marker> markers, IContext context, boolean isStatic);
}

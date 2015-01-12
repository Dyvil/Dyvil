package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.lexer.marker.Marker;

public interface IImport extends IASTNode, IContext
{
	public void resolveTypes(List<Marker> markers, IContext context);
	
	public boolean isValid();
}

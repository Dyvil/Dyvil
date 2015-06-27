package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public interface IMember extends INamed, ITyped, IModified, IAnnotationList
{
	public int getAccessLevel();
	
	// States
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
}

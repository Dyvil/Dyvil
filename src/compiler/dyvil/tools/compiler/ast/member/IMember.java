package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IMember extends IASTNode, INamed, ITyped, IModified, IAnnotated
{
	public int getAccessLevel();
	
	// States
	
	public void resolveTypes(MarkerList markers, IContext context);
	
	public void resolve(MarkerList markers, IContext context);
	
	public void checkTypes(MarkerList markers, IContext context);
	
	public void check(MarkerList markers, IContext context);
	
	public void foldConstants();
	
	public void cleanup(IContext context, IClassCompilableList compilableList);
}

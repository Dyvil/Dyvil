package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.modifiers.IModified;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IMember extends IASTNode, INamed, ITyped, IModified, IAnnotated
{
	int getAccessLevel();
	
	// States
	
	void resolveTypes(MarkerList markers, IContext context);
	
	void resolve(MarkerList markers, IContext context);
	
	void checkTypes(MarkerList markers, IContext context);
	
	void check(MarkerList markers, IContext context);
	
	void foldConstants();
	
	void cleanup(IContext context, IClassCompilableList compilableList);
}

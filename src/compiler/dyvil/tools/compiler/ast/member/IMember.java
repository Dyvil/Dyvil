package dyvil.tools.compiler.ast.member;

import dyvil.tools.compiler.phase.IResolvable;
import dyvil.tools.compiler.ast.annotation.IAnnotated;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.modifiers.IModified;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.parsing.ASTNode;
import dyvil.tools.parsing.marker.MarkerList;

public interface IMember extends ASTNode, IResolvable, INamed, ITyped, IModified, IAnnotated
{
	MemberKind getKind();

	int getAccessLevel();

	// States

	@Override
	void resolveTypes(MarkerList markers, IContext context);

	@Override
	void resolve(MarkerList markers, IContext context);

	@Override
	void checkTypes(MarkerList markers, IContext context);

	@Override
	void check(MarkerList markers, IContext context);

	@Override
	void foldConstants();

	@Override
	void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList);

	String getInternalName();
}

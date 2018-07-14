package dyvilx.tools.compiler.ast.member;

import dyvilx.tools.compiler.ast.attribute.Attributable;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.phase.IResolvable;
import dyvilx.tools.parsing.ASTNode;
import dyvilx.tools.parsing.marker.MarkerList;

public interface IMember extends ASTNode, IResolvable, Named, Typed, Attributable
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

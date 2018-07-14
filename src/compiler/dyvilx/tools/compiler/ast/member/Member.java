package dyvilx.tools.compiler.ast.member;

import dyvilx.tools.compiler.ast.attribute.Attributable;
import dyvilx.tools.compiler.ast.type.Typed;
import dyvilx.tools.compiler.phase.Resolvable;
import dyvilx.tools.parsing.ASTNode;

public interface Member extends ASTNode, Resolvable, Named, Typed, Attributable
{
	MemberKind getKind();

	int getAccessLevel();

	String getInternalName();
}

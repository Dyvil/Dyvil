package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.member.IClassMember;
import dyvilx.tools.compiler.ast.statement.MemberStatement;
import dyvilx.tools.compiler.ast.statement.StatementList;

public class FuncDirective extends MemberStatement
{
	public FuncDirective(IClassMember member)
	{
		super(member);
	}

	public void setBlock(StatementList block)
	{

	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		// TODO
		buffer.append('#');
		super.toString(indent, buffer);
	}
}

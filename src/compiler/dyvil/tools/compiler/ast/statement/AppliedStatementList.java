package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public class AppliedStatementList extends StatementList
{
	public AppliedStatementList()
	{
	}
	
	public AppliedStatementList(ICodePosition position)
	{
		this.position = position;
	}
}

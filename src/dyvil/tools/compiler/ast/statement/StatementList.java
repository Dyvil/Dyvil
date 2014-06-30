package dyvil.tools.compiler.ast.statement;

import java.util.ArrayList;
import java.util.List;

public class StatementList implements IStatement
{
	private List<IStatement> statements = new ArrayList();
	
	public void addStatement(IStatement statement)
	{
		this.statements.add(statement);
	}
	
	public List<IStatement> getStatements()
	{
		return this.statements;
	}
}

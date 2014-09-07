package dyvil.tools.compiler.ast.statement;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.IImplementable;

public class StatementList implements IStatement, IImplementable
{
	private List<IStatement>	statements	= new ArrayList();
	
	public void addStatement(IStatement statement)
	{
		this.statements.add(statement);
	}
	
	public List<IStatement> getStatements()
	{
		return this.statements;
	}
	
	@Override
	public void setStatement(IStatement statement)
	{
		this.statements.add(statement);
	}
	
	@Override
	public IStatement getStatement()
	{
		return this.statements.get(this.statements.size() - 1);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}

package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.statement.IStatement;

public interface IImplementable
{
	public void setStatement(IStatement statement);
	
	public IStatement getStatement();
	
	public default boolean hasStatement()
	{
		return this.getStatement() != null;
	}
}

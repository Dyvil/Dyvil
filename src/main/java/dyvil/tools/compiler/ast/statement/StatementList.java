package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.expression.ValueList;

public class StatementList extends ValueList implements IStatement
{
	public void addStatement(IStatement statement)
	{
		this.values.add(statement);
	}
}

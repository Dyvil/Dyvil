package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.value.IValue;

public class StatementList extends ValueList implements IStatement
{
	public void addStatement(IStatement statement)
	{
		this.values.add(statement);
	}
	
	@Override
	public IValue fold()
	{
		if (this.values.size() == 1)
		{
			return this.values.get(0).fold();
		}
		return this;
	}
}

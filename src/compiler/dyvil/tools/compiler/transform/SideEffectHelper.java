package dyvil.tools.compiler.transform;

import dyvil.tools.compiler.ast.expression.access.FieldAccess;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.statement.VariableStatement;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.parsing.Name;

public class SideEffectHelper
{
	private StatementList statementList;
	private int registered;

	public IValue processValue(IValue value)
	{
		if (value == null || !value.hasSideEffects())
		{
			return value;
		}

		if (this.statementList == null)
		{
			this.statementList = new StatementList();
		}

		final Variable variable = new Variable(value.getPosition(), Name.fromRaw("sideEffect$" + this.registered),
		                                       value.getType());
		variable.setValue(value);

		this.statementList.addValue(new VariableStatement(variable));
		this.statementList.addVariable(variable);
		this.registered++;

		return new FieldAccess(value.getPosition(), null, variable);
	}
	
	public IArguments processArguments(IArguments arguments)
	{
		final IArguments copy = arguments.copy();
		for (int i = 0, count = arguments.size(); i < count; i++)
		{
			final IValue value = arguments.getValue(i, null);
			copy.setValue(i, null, this.processValue(value));
		}
		return copy;
	}
	
	public IValue finish(IValue value)
	{
		if (this.statementList != null)
		{
			this.statementList.addValue(value);
			return this.statementList;
		}
		return value;
	}
}

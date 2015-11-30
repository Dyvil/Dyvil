package dyvil.tools.compiler.transform;

import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.statement.FieldInitializer;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.parsing.Name;

public class SideEffectHelper
{
	private StatementList statementList;
	private int registered = 0;

	public IValue processValue(IValue value)
	{
		if (!value.hasSideEffects())
		{
			return value;
		}

		if (this.statementList == null)
		{
			this.statementList = new StatementList();
		}

		final Variable variable = new Variable(value.getPosition(), Name.getQualified("sideEffect$" + registered),
		                                       value.getType());
		variable.setValue(value);

		this.statementList.addValue(new FieldInitializer(variable));
		registered++;

		return new FieldAccess(value.getPosition(), null, variable);
	}
	
	public IArguments processArguments(IArguments arguments)
	{
		for (int i = 0, count = arguments.size(); i < count; i++)
		{
			final IValue value = arguments.getValue(i, null);
			arguments.setValue(i, null, this.processValue(value));
		}
		return arguments;
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

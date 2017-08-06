package dyvilx.tools.compiler.transform;

import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.statement.VariableStatement;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvil.lang.Name;

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

		this.statementList.add(new VariableStatement(variable));
		this.statementList.addVariable(variable);
		this.registered++;

		return new FieldAccess(value.getPosition(), null, variable);
	}
	
	public ArgumentList processArguments(ArgumentList arguments)
	{
		final ArgumentList copy = arguments.copy();
		for (int i = 0, count = arguments.size(); i < count; i++)
		{
			final IValue value = arguments.get(i, null);
			copy.set(i, null, this.processValue(value));
		}
		return copy;
	}
	
	public IValue finish(IValue value)
	{
		if (this.statementList != null)
		{
			this.statementList.add(value);
			return this.statementList;
		}
		return value;
	}
}

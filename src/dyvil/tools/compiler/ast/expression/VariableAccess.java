package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class VariableAccess implements IValue
{
	public Method container;
	public Variable variable;
	
	public VariableAccess(Method method, Variable variable)
	{
		this.container = method;
		this.variable = variable;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public boolean isConstant()
	{
		return this.variable.getValue().isConstant();
	}

	@Override
	public Type getType()
	{
		return this.variable.getType();
	}
}

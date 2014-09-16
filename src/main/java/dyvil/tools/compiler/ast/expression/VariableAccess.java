package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.method.Method;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class VariableAccess implements IValue
{
	public Method container;
	public Field variable;
	
	public VariableAccess(Method method, Field variable)
	{
		this.container = method;
		this.variable = variable;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}

	@Override
	public Type getType()
	{
		return this.variable.getType();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}

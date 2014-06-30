package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.field.Variable;

public class ForEachStatement extends WhileStatement
{
	private Variable variable;
	private Object iterable;
	
	public ForEachStatement()
	{
	}
	
	public void setVariable(Variable variable)
	{
		this.variable = variable;
	}
	
	public void setIterable(Object iterable)
	{
		this.iterable = iterable;
	}
	
	public Variable getVariable()
	{
		return this.variable;
	}
	
	public Object getIterable()
	{
		return this.iterable;
	}
}

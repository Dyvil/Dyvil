package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.field.Field;

public class ForEachStatement extends WhileStatement
{
	private Field variable;
	private Object iterable;
	
	public ForEachStatement()
	{
	}
	
	public void setVariable(Field variable)
	{
		this.variable = variable;
	}
	
	public void setIterable(Object iterable)
	{
		this.iterable = iterable;
	}
	
	public Field getVariable()
	{
		return this.variable;
	}
	
	public Object getIterable()
	{
		return this.iterable;
	}
}

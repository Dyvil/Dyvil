package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.config.Formatting;

public class IfStatement implements IStatement, IValued
{
	private IValue		condition;
	private IStatement	then;
	private IStatement	elseThen;
	
	public IfStatement()
	{
	}
	
	@Override
	public void setValue(IValue condition)
	{
		this.condition = condition;
	}
	
	public void setThen(IStatement then)
	{
		this.then = then;
	}
	
	public void setElseThen(IStatement elseThen)
	{
		this.elseThen = elseThen;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
	}
	
	public IStatement getThen()
	{
		return this.then;
	}
	
	public IStatement getElseThen()
	{
		return this.elseThen;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(Formatting.Statements.ifStart);
		this.condition.toString(prefix, buffer);
		buffer.append(Formatting.Statements.ifEnd);
		// TODO Body
	}
}

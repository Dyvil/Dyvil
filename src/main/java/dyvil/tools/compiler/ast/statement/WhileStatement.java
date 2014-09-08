package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class WhileStatement implements IStatement, IValued
{
	private IValue	condition;
	private IValue	then;
	
	public WhileStatement()
	{}
	
	@Override
	public void setValue(IValue value)
	{
		this.condition = value;
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return this.then.getType();
	}
}

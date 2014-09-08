package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class DoWhileStatement implements IStatement, IValued
{
	private IValue	then;
	private IValue		condition;
	
	public DoWhileStatement()
	{
	}
	
	public void setThen(IValue then)
	{
		this.then = then;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.condition = value;
	}
	
	public IValue getThen()
	{
		return this.then;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
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

package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.value.IValue;

public class DoWhileStatement implements IStatement, IValued
{
	private IStatement	then;
	private IValue		condition;
	
	public DoWhileStatement()
	{
	}
	
	public void setThen(IStatement then)
	{
		this.then = then;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.condition = value;
	}
	
	public IStatement getThen()
	{
		return this.then;
	}
	
	@Override
	public IValue getValue()
	{
		return this.condition;
	}
}

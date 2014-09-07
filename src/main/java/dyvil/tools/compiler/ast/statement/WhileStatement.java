package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.value.IValue;

public class WhileStatement implements IStatement, IValued
{
	private IValue		condition;
	private IStatement	then;
	
	public WhileStatement()
	{
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.condition = value;
	}
	
	public void setThen(IStatement then)
	{
		this.then = then;
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
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}

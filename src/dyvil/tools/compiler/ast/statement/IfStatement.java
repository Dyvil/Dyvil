package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IField;

public class IfStatement implements IStatement, IField
{
	private Object		condition;
	private IStatement	then;
	private IStatement	elseThen;
	
	public IfStatement()
	{
	}
	
	@Override
	public void setValue(Object condition)
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
	public Object getValue()
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
}

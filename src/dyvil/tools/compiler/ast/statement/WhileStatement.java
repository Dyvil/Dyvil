package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IField;

public class WhileStatement implements IStatement, IField
{
	private Object		condition;
	private IStatement	then;
	
	public WhileStatement()
	{
	}
	
	@Override
	public void setValue(Object value)
	{
		this.condition = value;
	}
	
	public void setThen(IStatement then)
	{
		this.then = then;
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
}

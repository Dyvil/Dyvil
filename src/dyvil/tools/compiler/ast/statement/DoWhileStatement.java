package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.api.IField;

public class DoWhileStatement implements IStatement, IField
{
	private IStatement	then;
	private Object		condition;
	
	public DoWhileStatement()
	{
	}
	
	public void setThen(IStatement then)
	{
		this.then = then;
	}
	
	@Override
	public void setValue(Object value)
	{
		this.condition = value;
	}
	
	public IStatement getThen()
	{
		return this.then;
	}
	
	@Override
	public Object getValue()
	{
		return this.condition;
	}
}

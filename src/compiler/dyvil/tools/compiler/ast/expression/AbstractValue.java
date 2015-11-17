package dyvil.tools.compiler.ast.expression;

import dyvil.tools.parsing.position.ICodePosition;

public abstract class AbstractValue implements IValue
{
	protected ICodePosition position;
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
}

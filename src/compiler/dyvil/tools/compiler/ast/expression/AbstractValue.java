package dyvil.tools.compiler.ast.expression;

import dyvil.source.position.SourcePosition;

public abstract class AbstractValue implements IValue
{
	protected SourcePosition position;
	
	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}
}

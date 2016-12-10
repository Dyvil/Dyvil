package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public abstract class Pattern implements IPattern
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

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
}

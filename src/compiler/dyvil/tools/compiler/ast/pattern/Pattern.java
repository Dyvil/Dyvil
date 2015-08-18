package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.lexer.position.ICodePosition;

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
}

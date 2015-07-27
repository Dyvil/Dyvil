package dyvil.tools.compiler.ast.expression;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Value implements IValue
{
	protected ICodePosition position;
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
}

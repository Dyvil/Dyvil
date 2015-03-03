package dyvil.tools.compiler.ast;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IASTNode
{
	public default void setPosition(ICodePosition position)
	{
	}
	
	public default ICodePosition getPosition()
	{
		return null;
	}
	
	public default void expandPosition(ICodePosition position)
	{
	}
	
	public void toString(String prefix, StringBuilder buffer);
}

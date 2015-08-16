package dyvil.tools.compiler.ast;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IASTNode
{
	public default void setPosition(ICodePosition position)
	{
	}
	
	public ICodePosition getPosition();
	
	public default int getLineNumber()
	{
		ICodePosition position = this.getPosition();
		return position == null ? 0 : position.startLine();
	}
	
	public default void expandPosition(ICodePosition position)
	{
	}
	
	public static String toString(IASTNode node)
	{
		StringBuilder s = new StringBuilder();
		node.toString("", s);
		return s.toString();
	}
	
	public void toString(String prefix, StringBuilder buffer);
}

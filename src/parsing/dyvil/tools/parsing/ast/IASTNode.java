package dyvil.tools.parsing.ast;

import dyvil.tools.parsing.position.ICodePosition;

public interface IASTNode
{
	void setPosition(ICodePosition position);
	
	ICodePosition getPosition();
	
	default int getLineNumber()
	{
		ICodePosition position = this.getPosition();
		return position == null ? 0 : position.startLine();
	}
	
	default void expandPosition(ICodePosition position)
	{
		ICodePosition pos = this.getPosition();
		if (pos == null)
		{
			this.setPosition(position);
			return;
		}
		this.setPosition(pos.to(position));
	}
	
	static String toString(IASTNode node)
	{
		StringBuilder s = new StringBuilder();
		node.toString("", s);
		return s.toString();
	}
	
	void toString(String prefix, StringBuilder buffer);
}

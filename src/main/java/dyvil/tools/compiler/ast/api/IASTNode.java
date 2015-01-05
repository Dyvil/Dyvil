package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IASTNode
{
	public void setPosition(ICodePosition position);
	
	public ICodePosition getPosition();
	
	public void expandPosition(ICodePosition position);
	
	public void toString(String prefix, StringBuilder buffer);
}

package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class CharPattern extends ASTNode implements IPattern
{
	private char	value;
	
	public CharPattern(ICodePosition position, char value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('\'').append(this.value).append('\'');
	}
}

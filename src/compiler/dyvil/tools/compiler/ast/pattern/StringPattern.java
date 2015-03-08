package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StringPattern extends ASTNode implements IPattern
{
	private String	value;
	
	public StringPattern(ICodePosition position, String value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}

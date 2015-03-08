package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class LongPattern extends ASTNode implements IPattern
{
	private long	value;
	
	public LongPattern(ICodePosition position, long value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}
}

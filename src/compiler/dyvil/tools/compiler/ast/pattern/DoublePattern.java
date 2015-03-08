package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class DoublePattern extends ASTNode implements IPattern
{
	private double	value;
	
	public DoublePattern(ICodePosition position, double value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}

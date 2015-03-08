package dyvil.tools.compiler.ast.pattern;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class BooleanPattern extends ASTNode implements IPattern
{
	private boolean	value;
	
	public BooleanPattern(ICodePosition position, boolean value)
	{
		this.position = position;
		this.value = value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

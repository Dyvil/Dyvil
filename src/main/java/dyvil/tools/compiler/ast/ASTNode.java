package dyvil.tools.compiler.ast;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IASTNode;
import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class ASTNode implements IASTNode
{
	protected ICodePosition	position;
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void expandPosition(ICodePosition position)
	{
		if (this.position != null)
		{
			this.position = this.position.to(position);
		}
	}
	
	@Override
	public abstract IASTNode applyState(CompilerState state, IContext context);
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		this.toString("", buffer);
		return buffer.toString();
	}
	
	@Override
	public abstract void toString(String prefix, StringBuilder buffer);
}

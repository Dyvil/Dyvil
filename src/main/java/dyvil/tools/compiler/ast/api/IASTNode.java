package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public interface IASTNode
{
	public void setPosition(ICodePosition position);
	
	public ICodePosition getPosition();
	
	public void expandPosition(ICodePosition position);
	
	public IASTNode applyState(CompilerState state, IContext context);
	
	public void toString(String prefix, StringBuilder buffer);
}

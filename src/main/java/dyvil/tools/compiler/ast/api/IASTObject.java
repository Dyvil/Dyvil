package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.CompilerState;

public interface IASTObject
{
	public void applyState(CompilerState state);
	
	public void toString(String prefix, StringBuilder buffer);
}

package dyvil.tools.compiler.ast;

import dyvil.tools.compiler.CompilerState;

public abstract class ASTObject
{
	public abstract void applyState(CompilerState state);
	
	@Override
	public String toString()
	{
		StringBuilder buffer = new StringBuilder();
		this.toString("", buffer);
		return buffer.toString();
	}
	
	public abstract void toString(String prefix, StringBuilder buffer);
}

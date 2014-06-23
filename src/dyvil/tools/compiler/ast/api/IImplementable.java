package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.codeblock.CodeBlock;

public interface IImplementable
{
	public void setImplementation(CodeBlock implementation);
	
	public CodeBlock getImplementation();
	
	public default boolean hasImplementation()
	{
		return this.getImplementation() != null;
	}
}

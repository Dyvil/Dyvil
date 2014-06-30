package dyvil.tools.compiler.ast.api;

import dyvil.tools.compiler.ast.codeblock.CodeBlock;

public interface IImplementable
{
	public void setCodeBlock(CodeBlock block);
	
	public CodeBlock getCodeBlock();
	
	public default boolean hasCodeBlock()
	{
		return this.getCodeBlock() != null;
	}
}

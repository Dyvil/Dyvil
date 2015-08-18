package dyvil.tools.compiler.ast.imports;

import dyvil.tools.compiler.lexer.position.ICodePosition;

public abstract class Import implements IImport
{
	protected ICodePosition	position;
	protected IImport		parent;
	
	public Import(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void setParent(IImport parent)
	{
		this.parent = parent;
	}
	
	@Override
	public IImport getParent()
	{
		return this.parent;
	}
	
	public void appendParent(String prefix, StringBuilder builder)
	{
		if (this.parent != null)
		{
			this.parent.toString(prefix, builder);
			builder.append('.');
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		this.toString("", sb);
		return sb.toString();
	}
}

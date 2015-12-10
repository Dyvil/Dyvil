package dyvil.tools.parsing.token;

import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public class EndToken implements IToken
{
	private int    index;
	private int    line;
	private IToken prev;
	
	public EndToken(int index, int line)
	{
		this.index = index;
		this.line = line;
	}
	
	@Override
	public int startIndex()
	{
		return this.index;
	}
	
	@Override
	public int endIndex()
	{
		return this.index;
	}
	
	@Override
	public int startLine()
	{
		return this.line;
	}
	
	@Override
	public int endLine()
	{
		return this.line;
	}
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.line, this.index, this.index);
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.line, end.endLine(), this.index, end.endIndex());
	}
	
	@Override
	public int type()
	{
		return 0;
	}
	
	@Override
	public IToken prev()
	{
		return this.prev;
	}
	
	@Override
	public IToken next()
	{
		return this;
	}
	
	@Override
	public void setPrev(IToken prev)
	{
		this.prev = prev;
	}
	
	@Override
	public void setNext(IToken next)
	{
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev.type() != 0;
	}
	
	@Override
	public boolean hasNext()
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		return "EOF";
	}
}

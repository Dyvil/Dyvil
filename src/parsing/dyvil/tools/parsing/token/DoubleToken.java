package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public final class DoubleToken implements IToken
{
	private IToken	prev;
	private IToken	next;
	
	private final int	lineNumber;
	private final int	start;
	private final int	end;
	
	private double value;
	
	public DoubleToken(IToken prev, double value, int lineNumber, int start, int end)
	{
		this.prev = prev;
		prev.setNext(this);
		this.value = value;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	public DoubleToken(double value, int lineNumber, int start, int end)
	{
		this.value = value;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public int type()
	{
		return Tokens.DOUBLE;
	}
	
	@Override
	public double doubleValue()
	{
		return this.value;
	}
	
	@Override
	public int startIndex()
	{
		return this.start;
	}
	
	@Override
	public int endIndex()
	{
		return this.end;
	}
	
	@Override
	public int startLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public int endLine()
	{
		return this.lineNumber;
	}
	
	@Override
	public void setPrev(IToken prev)
	{
		this.prev = prev;
	}
	
	@Override
	public void setNext(IToken next)
	{
		this.next = next;
	}
	
	@Override
	public IToken prev()
	{
		return this.prev;
	}
	
	@Override
	public IToken next()
	{
		return this.next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next.type() != 0;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev.type() != 0;
	}
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.endIndex());
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Double " + this.value;
	}
}

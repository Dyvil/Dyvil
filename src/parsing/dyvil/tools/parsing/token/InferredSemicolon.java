package dyvil.tools.parsing.token;

import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public class InferredSemicolon implements IToken
{
	public IToken	prev;
	public IToken	next;
	
	public final int	lineNumber;
	public final int	start;
	
	public InferredSemicolon(int lineNumber, int start)
	{
		this.lineNumber = lineNumber;
		this.start = start;
	}
	
	@Override
	public boolean isInferred()
	{
		return true;
	}
	
	@Override
	public int type()
	{
		return Symbols.SEMICOLON;
	}
	
	@Override
	public int startIndex()
	{
		return this.start;
	}
	
	@Override
	public int endIndex()
	{
		return this.start + 1;
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
		return this.next != null;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev != null;
	}
	
	@Override
	public ICodePosition raw()
	{
		return new CodePosition(this.lineNumber, this.start, this.start + 1);
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Inferred Semicolon";
	}
}

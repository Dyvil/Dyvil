package dyvil.tools.compiler.lexer.token;

import java.util.Objects;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Keywords;

public final class KeywordToken implements IToken
{
	private IToken		prev;
	private IToken		next;
	
	private final int	type;
	
	private final int	lineNumber;
	private final int	start;
	private final int	end;
	
	public KeywordToken(IToken prev, int type, int lineNumber, int start, int end)
	{
		this.prev = prev;
		prev.setNext(this);
		this.type = type;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	public KeywordToken(String value, int type, int lineNumber, int start, int end)
	{
		this.type = type;
		
		this.lineNumber = lineNumber;
		this.start = start;
		this.end = end;
	}
	
	@Override
	public String text()
	{
		return Keywords.keywordToString(this.type);
	}
	
	@Override
	public int type()
	{
		return this.type;
	}
	
	@Override
	public boolean equals(String value)
	{
		return Objects.equals(Keywords.keywordToString(this.type), value);
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
	public IToken prev() throws SyntaxError
	{
		if (this.prev == null)
		{
			throw new SyntaxError(this, "Unexpected End of Input");
		}
		return this.prev;
	}
	
	@Override
	public IToken next() throws SyntaxError
	{
		if (this.next == null)
		{
			throw new SyntaxError(this, "Unexpected End of Input");
		}
		return this.next;
	}
	
	@Override
	public IToken getPrev()
	{
		return this.prev;
	}
	
	@Override
	public IToken getNext()
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
		return new CodePosition(this.lineNumber, this.start, this.end);
	}
	
	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(this.lineNumber, end.endLine(), this.start, end.endIndex());
	}
	
	@Override
	public String toString()
	{
		return "Keyword '" + Keywords.keywordToString(this.type) + "' (line " + this.lineNumber + ")";
	}
}

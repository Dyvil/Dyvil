package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.Symbols;

public final class SymbolToken implements IToken
{
	private IToken	prev;
	private IToken	next;
	
	private final int type;
	
	private final int	lineNumber;
	private final int	start;
	
	public SymbolToken(IToken prev, int type, int lineNumber, int start)
	{
		this.prev = prev;
		prev.setNext(this);
		this.type = type;
		
		this.lineNumber = lineNumber;
		this.start = start;
	}
	
	public SymbolToken(String value, int type, int lineNumber, int start)
	{
		this.type = type;
		
		this.lineNumber = lineNumber;
		this.start = start;
	}
	
	@Override
	public int type()
	{
		return this.type;
	}
	
	@Override
	public int startIndex()
	{
		return this.start;
	}
	
	@Override
	public int endIndex()
	{
		if (this.type == Symbols.ARROW_OPERATOR)
		{
			return this.start + 2;
		}
		if (this.type == Symbols.ELLIPSIS)
		{
			return this.start + 3;
		}
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
		return "Symbol '" + Symbols.symbolToString(this.type) + "' (line " + this.lineNumber + ")";
	}
}

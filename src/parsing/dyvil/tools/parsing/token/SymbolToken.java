package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.Symbols;
import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public final class SymbolToken implements IToken
{
	private Symbols symbols;
	
	private IToken prev;
	private IToken next;
	
	private final int type;
	
	private final int lineNumber;
	private final int start;
	
	public SymbolToken(Symbols symbols, IToken prev, int type, int lineNumber, int start)
	{
		this.symbols = symbols;
		this.prev = prev;
		prev.setNext(this);
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
		return this.start + this.symbols.getLength(this.type);
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
		return "Symbol '" + this.symbols.toString(this.type) + '\'';
	}
}

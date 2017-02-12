package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.Symbols;

public final class SymbolToken implements IToken
{
	private Symbols symbols;

	private IToken prev;
	private IToken next;

	private final int type;

	private final int lineNumber;
	private final int startColumn;

	public SymbolToken(Symbols symbols, int type, int lineNumber, int startColumn)
	{
		this.symbols = symbols;
		this.type = type;
		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
	}

	public SymbolToken(Symbols symbols, IToken prev, int type, int lineNumber, int startColumn)
	{
		this.symbols = symbols;
		this.prev = prev;
		prev.setNext(this);
		this.type = type;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
	}

	@Override
	public int type()
	{
		return this.type;
	}

	@Override
	public String stringValue()
	{
		return this.symbols.toString(this.type);
	}

	@Override
	public int startColumn()
	{
		return this.startColumn;
	}

	@Override
	public int endColumn()
	{
		return this.startColumn + this.symbols.getLength(this.type);
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
	public String toString()
	{
		return "Symbol '" + this.symbols.toString(this.type) + '\'';
	}
}

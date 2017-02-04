package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.Tokens;

public final class LongToken implements IToken
{
	private IToken prev;
	private IToken next;

	private final int lineNumber;
	private final int startColumn;
	private final int endColumn;

	private long value;

	public LongToken(IToken prev, int lineNumber, int startColumn, int endColumn)
	{
		this.prev = prev;
		prev.setNext(this);

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public LongToken(IToken prev, long value, int lineNumber, int startColumn, int endColumn)
	{
		this.prev = prev;
		prev.setNext(this);
		this.value = value;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public LongToken(long value, int lineNumber, int startColumn, int endColumn)
	{
		this.value = value;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	@Override
	public int type()
	{
		return Tokens.LONG;
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	@Override
	public long longValue()
	{
		return this.value;
	}

	@Override
	public int startColumn()
	{
		return this.startColumn;
	}

	@Override
	public int endColumn()
	{
		return this.endColumn;
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
		return "Long " + this.value;
	}
}

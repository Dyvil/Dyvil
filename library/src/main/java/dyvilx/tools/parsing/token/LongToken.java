package dyvilx.tools.parsing.token;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.parsing.lexer.Tokens;

public final class LongToken implements IToken
{
	private @NonNull IToken prev;
	private @NonNull IToken next;

	private final int lineNumber;
	private final int startColumn;
	private final int endColumn;

	private long value;

	public LongToken(@NonNull IToken prev, int lineNumber, int startColumn, int endColumn)
	{
		this.prev = prev;
		prev.setNext(this);

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public LongToken(@NonNull IToken prev, long value, int lineNumber, int startColumn, int endColumn)
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
	public void setPrev(@NonNull IToken prev)
	{
		this.prev = prev;
	}

	@Override
	public void setNext(@NonNull IToken next)
	{
		this.next = next;
	}

	@Override
	public @NonNull IToken prev()
	{
		return this.prev;
	}

	@Override
	public @NonNull IToken next()
	{
		return this.next;
	}

	@Override
	public String toString()
	{
		return "Long " + this.value;
	}
}

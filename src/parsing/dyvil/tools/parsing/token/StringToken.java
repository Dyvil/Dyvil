package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.Tokens;

public final class StringToken implements IToken
{
	private IToken prev;
	private IToken next;

	private final int type;
	private final int startLine;
	private final int endLine;
	private final int startColumn;
	private final int endColumn;

	private final String value;

	public StringToken(String value, int type, int startLine, int endLine, int startColumn, int endColumn)
	{
		this.value = value;

		this.type = type;
		this.startLine = startLine;
		this.endLine = endLine;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	@Override
	public int type()
	{
		return this.type;
	}

	@Override
	public String stringValue()
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
		return this.startLine;
	}

	@Override
	public int endLine()
	{
		return this.endLine;
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
	public String toString()
	{
		String s = '"' + this.value + '"';

		switch (this.type)
		{
		case Tokens.STRING_START:
			return "String Start " + s;
		case Tokens.STRING_PART:
			return "String Part " + s;
		case Tokens.STRING_END:
			return "String End " + s;
		}
		return "String " + s;
	}
}

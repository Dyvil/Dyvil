package dyvil.tools.parsing.token;

import dyvil.tools.parsing.lexer.BaseSymbols;

public class InferredSemicolon implements IToken
{
	public IToken prev;
	public IToken next;

	public final int lineNumber;
	public final int startColumn;

	public InferredSemicolon(int lineNumber, int startColumn)
	{
		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
	}

	@Override
	public boolean isInferred()
	{
		return true;
	}

	@Override
	public int type()
	{
		return BaseSymbols.SEMICOLON;
	}

	@Override
	public int startColumn()
	{
		return this.startColumn;
	}

	@Override
	public int endColumn()
	{
		return this.startColumn + 1;
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
		return "Inferred Semicolon";
	}
}

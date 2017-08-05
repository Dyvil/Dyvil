package dyvilx.tools.parsing.token;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.parsing.lexer.BaseSymbols;

public class InferredSemicolon implements IToken
{
	public @NonNull IToken prev;
	public @NonNull IToken next;

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
		return "Inferred Semicolon";
	}
}

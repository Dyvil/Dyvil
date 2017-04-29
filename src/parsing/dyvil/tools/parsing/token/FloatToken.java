package dyvil.tools.parsing.token;

import dyvil.annotation.internal.NonNull;
import dyvil.tools.parsing.lexer.Tokens;

public final class FloatToken implements IToken
{
	private @NonNull IToken prev;
	private @NonNull IToken next;

	private final int lineNumber;
	private final int startColumn;
	private final int endColumn;

	private float value;

	public FloatToken(@NonNull IToken prev, float value, int lineNumber, int startColumn, int endColumn)
	{
		this.prev = prev;
		prev.setNext(this);
		this.value = value;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public FloatToken(float value, int lineNumber, int startColumn, int endColumn)
	{
		this.value = value;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	@Override
	public int type()
	{
		return Tokens.FLOAT;
	}

	public void setValue(float value)
	{
		this.value = value;
	}

	@Override
	public float floatValue()
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
		return "Float " + this.value;
	}
}

package dyvilx.tools.parsing.token;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;

public class IdentifierToken implements IToken
{
	public @NonNull IToken prev;
	public @NonNull IToken next;

	public final int  type;
	public final Name name;

	public final int lineNumber;
	public final int startColumn;
	public final int endColumn;

	public IdentifierToken(@NonNull IToken prev, Name name, int type, int lineNumber, int startColumn, int endColumn)
	{
		this.prev = prev;
		prev.setNext(this);
		this.name = name;
		this.type = type;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	public IdentifierToken(Name name, int type, int lineNumber, int startColumn, int endColumn)
	{
		this.name = name;
		this.type = type;

		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
	}

	@Override
	public int type()
	{
		return this.type;
	}

	@Override
	public Name nameValue()
	{
		return this.name;
	}

	@Override
	public String stringValue()
	{
		return this.name.unqualified;
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
		return "Identifier '" + this.name + "\'";
	}
}

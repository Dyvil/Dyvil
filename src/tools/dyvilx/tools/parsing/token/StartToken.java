package dyvilx.tools.parsing.token;

import dyvil.annotation.internal.NonNull;

public class StartToken implements IToken
{
	private @NonNull IToken next;

	@Override
	public int startColumn()
	{
		return 0;
	}

	@Override
	public int endColumn()
	{
		return 0;
	}

	@Override
	public int startLine()
	{
		return 1;
	}

	@Override
	public int endLine()
	{
		return 1;
	}

	@Override
	public int type()
	{
		return 0;
	}

	@Override
	public @NonNull IToken prev()
	{
		return this;
	}

	@Override
	public @NonNull IToken next()
	{
		return this.next;
	}

	@Override
	public void setPrev(@NonNull IToken prev)
	{
	}

	@Override
	public void setNext(@NonNull IToken next)
	{
		this.next = next;
	}
}

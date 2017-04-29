package dyvil.tools.parsing.token;

public class StartToken implements IToken
{
	private IToken next;

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
	public IToken prev()
	{
		return this;
	}

	@Override
	public IToken next()
	{
		return this.next;
	}

	@Override
	public void setPrev(IToken prev)
	{
	}

	@Override
	public void setNext(IToken next)
	{
		this.next = next;
	}
}

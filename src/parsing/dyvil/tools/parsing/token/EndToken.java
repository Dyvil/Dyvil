package dyvil.tools.parsing.token;

public class EndToken implements IToken
{
	private int    index;
	private int    line;
	private IToken prev;

	public EndToken(int index, int line)
	{
		this.index = index;
		this.line = line;
	}

	@Override
	public int startColumn()
	{
		return this.index;
	}

	@Override
	public int endColumn()
	{
		return this.index;
	}

	@Override
	public int startLine()
	{
		return this.line;
	}

	@Override
	public int endLine()
	{
		return this.line;
	}

	@Override
	public int type()
	{
		return 0;
	}

	@Override
	public IToken prev()
	{
		return this.prev;
	}

	@Override
	public IToken next()
	{
		return this;
	}

	@Override
	public void setPrev(IToken prev)
	{
		this.prev = prev;
	}

	@Override
	public void setNext(IToken next)
	{
	}

	@Override
	public String toString()
	{
		return "EOF";
	}
}

package dyvil.tools.parsing.token;

import dyvil.tools.parsing.position.CodePosition;
import dyvil.tools.parsing.position.ICodePosition;

public class StartToken implements IToken
{
	private IToken next;
	
	@Override
	public int startIndex()
	{
		return 0;
	}

	@Override
	public int endIndex()
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
	public ICodePosition raw()
	{
		return CodePosition.ORIGIN;
	}

	@Override
	public ICodePosition to(ICodePosition end)
	{
		return new CodePosition(1, end.endLine(), 0, end.endIndex());
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

	@Override
	public boolean hasPrev()
	{
		return false;
	}

	@Override
	public boolean hasNext()
	{
		return this.next.type() != 0;
	}
}

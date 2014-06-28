package dyvil.tools.compiler.lexer.token;

import dyvil.tools.compiler.lexer.SyntaxException;

public class FakeToken implements IToken
{
	private IToken	prev;
	private IToken	next;
	
	@Override
	public String value() throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public byte type() throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public boolean equals(String value) throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public boolean isType(byte type) throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public int index() throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public int start() throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public int end() throws SyntaxException
	{
		throw new SyntaxException("No token!");
	}
	
	@Override
	public IToken next() throws SyntaxException
	{
		if (this.next == null)
		{
			throw new SyntaxException("No next token!");
		}
		return this.next;
	}
	
	@Override
	public IToken prev() throws SyntaxException
	{
		if (this.prev == null)
		{
			throw new SyntaxException("No prev token!");
		}
		return this.prev;
	}
	
	@Override
	public void setIndex(int index)
	{
	}
	
	@Override
	public void setPrev(IToken token)
	{
		this.prev = token;
	}
	
	@Override
	public void setNext(IToken token)
	{
		this.next = token;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next != null;
	}
	
	@Override
	public boolean hasPrev()
	{
		return this.prev != null;
	}
}
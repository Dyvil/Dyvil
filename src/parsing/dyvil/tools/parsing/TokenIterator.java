package dyvil.tools.parsing;

import java.util.Iterator;
import java.util.NoSuchElementException;

import dyvil.tools.parsing.token.IToken;

public class TokenIterator implements Iterator<IToken>
{
	protected final IToken	first;
	
	protected IToken		lastReturned;
	protected IToken		next;
	
	public TokenIterator(IToken first)
	{
		this.first = first;
		this.next = first;
	}
	
	public void reset()
	{
		this.lastReturned = null;
		this.next = this.first;
	}
	
	public void jump(IToken next)
	{
		this.lastReturned = null;
		this.next = next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next != null;
	}
	
	public IToken first()
	{
		return this.first;
	}
	
	public IToken lastReturned()
	{
		return this.lastReturned;
	}
	
	@Override
	public IToken next()
	{
		if (this.next == null)
		{
			throw new NoSuchElementException();
		}
		
		this.lastReturned = this.next;
		this.next = this.next.next();
		return this.lastReturned;
	}
	
	@Override
	public void remove()
	{
		IToken prev = this.lastReturned;
		if (prev == null)
		{
			throw new IllegalStateException();
		}
		
		IToken next = this.next.next();
		
		prev.setNext(next);
		next.setPrev(prev);
		this.lastReturned = null;
		this.next = next;
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		IToken token = this.first;
		while (token != null)
		{
			buf.append(token).append('\n');
			token = token.next();
		}
		return buf.toString();
	}
}

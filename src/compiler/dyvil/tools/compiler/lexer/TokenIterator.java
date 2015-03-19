package dyvil.tools.compiler.lexer;

import java.util.Iterator;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public class TokenIterator implements Iterator<IToken>
{
	protected IToken	first;
	protected IToken	lastReturned;
	protected IToken	next;
	
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
		this.lastReturned = next.getPrev();
		this.next = next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.lastReturned != null ? this.lastReturned.hasNext() : this.next != null;
	}
	
	@Override
	public IToken next()
	{
		this.lastReturned = this.next;
		if (this.next != null)
		{
			this.next = this.next.getNext();
		}
		return this.lastReturned;
	}
	
	@Override
	public void remove()
	{
		try
		{
			IToken prev = this.lastReturned;
			IToken next = this.next.next();
			
			prev.setNext(next);
			next.setPrev(prev);
			this.lastReturned = prev;
			this.next = next;
		}
		catch (SyntaxError ex)
		{
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		IToken token = this.first;
		while (token != null)
		{
			buf.append(token).append('\n');
			token = token.getNext();
		}
		return buf.toString();
	}
}

package dyvil.tools.parsing;

import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;
import dyvil.tools.parsing.token.StartToken;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TokenIterator implements Iterator<IToken>
{
	protected final IToken startToken;
	protected IToken       lastReturned;
	protected IToken       next;

	public TokenIterator()
	{
		this.startToken = this.next = new StartToken();
	}

	public void reset()
	{
		this.lastReturned = null;
		this.next = this.startToken.next();
	}

	public void setNext(IToken next)
	{
		this.lastReturned = null;
		this.next = next;
	}

	public void append(IToken token)
	{
		token.setPrev(this.next);
		this.next.setNext(token);
		this.next = token;
	}

	@Override
	public boolean hasNext()
	{
		return this.next.type() != Tokens.EOF;
	}

	public IToken first()
	{
		return this.startToken.next();
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
		for (IToken token = this.first(); token.type() != Tokens.EOF; token = token.next())
		{
			buf.append(token).append('\n');
		}
		return buf.toString();
	}
}

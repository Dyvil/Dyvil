package dyvilx.tools.parsing;

import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class TokenIterator implements Iterator<IToken>
{
	protected IToken lastReturned;
	protected IToken next;

	public TokenIterator(IToken first)
	{
		this.next = first;
	}

	public void setNext(IToken next)
	{
		this.lastReturned = null;
		this.next = next;
	}

	@Override
	public boolean hasNext()
	{
		return this.next.type() != Tokens.EOF;
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
}

package dyvil.tools.compiler.lexer;

import java.util.Iterator;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;

public class TokenIterator implements Iterator<IToken>
{
	protected IToken	first;
	protected IToken	next;
	
	public TokenIterator(IToken first)
	{
		this.first = first;
		this.next = first;
	}
	
	public void reset()
	{
		this.next = this.first;
	}
	
	public void jump(IToken next)
	{
		this.next = next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.next instanceof Token;
	}
	
	@Override
	public IToken next()
	{
		try
		{
			IToken next = this.next;
			this.next = next.next();
			return next;
		}
		catch (SyntaxError ex)
		{
			return null;
		}
	}
	
	@Override
	public void remove()
	{
		try
		{
			IToken prev = this.next.prev();
			IToken next = this.next.next();
			
			prev.setNext(next);
			next.setPrev(prev);
			this.next = next;
		}
		catch (SyntaxError ex)
		{
		}
	}
}

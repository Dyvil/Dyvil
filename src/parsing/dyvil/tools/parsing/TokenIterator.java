package dyvil.tools.parsing;

import java.util.Iterator;

import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;
import dyvil.tools.parsing.token.InferredSemicolon;

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
		this.lastReturned = next.prev();
		this.next = next;
	}
	
	@Override
	public boolean hasNext()
	{
		return this.lastReturned != null ? this.lastReturned.hasNext() : this.next != null;
	}
	
	public IToken current()
	{
		return this.next;
	}
	
	@Override
	public IToken next()
	{
		this.lastReturned = this.next;
		if (this.next != null)
		{
			this.next = this.next.next();
		}
		return this.lastReturned;
	}
	
	@Override
	public void remove()
	{
		IToken prev = this.lastReturned;
		IToken next = this.next.next();
		
		prev.setNext(next);
		next.setPrev(prev);
		this.lastReturned = prev;
		this.next = next;
	}
	
	public void set(IToken current)
	{
		if (this.next != null)
		{
			current.setNext(this.next);
			this.next.setPrev(current);
		}
		IToken prev = this.lastReturned.prev();
		if (prev != null)
		{
			current.setPrev(prev);
			prev.setNext(current);
		}
		this.lastReturned = current;
	}
	
	public void inferSemicolons()
	{
		if (this.first == null)
		{
			return;
		}
		
		IToken next = this.first.next();
		IToken prev = this.first;
		while (next != null)
		{
			this.inferSemicolon(prev, next);
			prev = next;
			next = next.next();
		}
		
		next = this.first.next();
		prev = this.first;
		while (next != null)
		{
			next.setPrev(prev);
			prev = next;
			next = next.next();
		}
		
		prev.setNext(new InferredSemicolon(prev.endLine(), prev.endIndex() + 1));
		
		this.reset();
	}
	
	private void inferSemicolon(IToken prev, IToken next)
	{
		if (prev == null)
		{
			return;
		}
		
		int prevLN = prev.endLine();
		if (prevLN == next.startLine())
		{
			return;
		}
		
		int prevType = prev.type();
		switch (prevType)
		{
		case BaseSymbols.DOT:
		case BaseSymbols.COMMA:
		case BaseSymbols.COLON:
		case BaseSymbols.SEMICOLON:
		case BaseSymbols.OPEN_CURLY_BRACKET:
		case BaseSymbols.OPEN_PARENTHESIS:
		case BaseSymbols.OPEN_SQUARE_BRACKET:
		case Tokens.STRING_PART:
		case Tokens.STRING_START:
			return;
		}
		
		int nextType = next.type();
		if (nextType == BaseSymbols.OPEN_CURLY_BRACKET)
		{
			return;
		}
		
		int prevEnd = prev.endIndex();
		IToken semicolon = new InferredSemicolon(prevLN, prevEnd);
		semicolon.setNext(next);
		prev.setNext(semicolon);
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

package dyvil.tools.compiler.lexer;

import java.util.Iterator;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.InferredSemicolon;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;
import dyvil.tools.compiler.util.ParserUtil;

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
	
	public void inferSemicolons()
	{
		if (this.first == null)
		{
			return;
		}
		
		IToken token = this.first.getNext();
		IToken prev = this.first;
		while (token != null)
		{
			this.inferSemicolon(token, prev);
			prev = token;
			token = token.getNext();
		}
		
		token = this.first.getNext();
		prev = this.first;
		while (token != null)
		{
			token.setPrev(prev);
			prev = token;
			token = token.getNext();
		}
	}
	
	private void inferSemicolon(IToken token, IToken prev)
	{
		if (prev == null)
		{
			return;
		}
		
		int prevLN = prev.endLine();
		if (prevLN == token.startLine())
		{
			return;
		}
		
		int type = token.type();
		if ((type & (Tokens.SYMBOL | Tokens.KEYWORD | Tokens.IDENTIFIER | Tokens.BRACKET)) == 0)
		{
			return;
		}
		
		if (type == Symbols.OPEN_CURLY_BRACKET)
		{
			return;
		}
		
		int type1 = prev.type();
		if (ParserUtil.isSeperator(type1))
		{
			return;
		}
		
		int prevEnd = prev.endIndex();
		IToken semicolon = new InferredSemicolon(prevLN, prevEnd);
		semicolon.setNext(token);
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
			token = token.getNext();
		}
		return buf.toString();
	}
}

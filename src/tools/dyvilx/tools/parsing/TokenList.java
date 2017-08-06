package dyvilx.tools.parsing;

import dyvilx.tools.parsing.token.IToken;
import dyvilx.tools.parsing.token.StartToken;

public class TokenList implements Iterable<IToken>
{
	protected final IToken startToken;
	protected       IToken endToken;

	public TokenList()
	{
		this.startToken = this.endToken = new StartToken();
	}

	public void append(IToken token)
	{
		token.setPrev(this.endToken);
		this.endToken.setNext(token);
		this.endToken = token;
	}

	public void addAll(TokenList tokens)
	{
		this.append(tokens.first());
		this.endToken = tokens.last();
	}

	public IToken first()
	{
		return this.startToken.next();
	}

	public IToken last()
	{
		return this.endToken.prev();
	}

	@Override
	public TokenIterator iterator()
	{
		return new TokenIterator(this.first());
	}

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		for (IToken token : this)
		{
			buf.append(token).append('\n');
		}
		return buf.toString();
	}
}

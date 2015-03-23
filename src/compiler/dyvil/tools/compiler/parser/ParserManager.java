package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.InferredSemicolon;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ParserManager implements IParserManager
{
	protected Parser		parser;
	
	public boolean			semicolonInference;
	
	protected TokenIterator	tokens;
	protected int			skip;
	protected boolean		reparse;
	
	public ParserManager()
	{
	}
	
	/**
	 * Creates a new {@link ParserManager} with the given {@link Parser}
	 * {@code parser} as the current parser, and calls the parser's
	 * {@link Parser#begin(ParserManager) begin} method.
	 * 
	 * @see Parser#begin(ParserManager)
	 * @param parser
	 *            the parser
	 */
	public ParserManager(Parser parser)
	{
		this.parser = parser;
	}
	
	public final void parse(MarkerList markers, TokenIterator tokens)
	{
		this.tokens = tokens;
		
		IToken token = null;
		IToken prev = null;
		while (tokens.hasNext())
		{
			token = tokens.next();
			if (!this.retainToken(token, prev))
			{
				tokens.remove();
			}
			prev = token;
		}
		
		tokens.reset();
		while (tokens.hasNext())
		{
			token = tokens.next();
			token.setPrev(prev);
			prev = token;
		}
		
		tokens.reset();
		while (true)
		{
			if (this.reparse)
			{
				this.reparse = false;
			}
			else
			{
				token = tokens.next();
				
				if (token == null)
				{
					break;
				}
			}
			
			if (this.skip > 0)
			{
				this.skip--;
				continue;
			}
			
			try
			{
				this.parser.parse(this, token);
			}
			catch (SyntaxError ex)
			{
				// if (this.jumpBackToken != null)
				// {
				// tokens.jump(this.jumpBackToken);
				// this.popParser();
				// this.jumpBackToken = null;
				// }
				// else
				{
					if (ex.reparse)
					{
						this.reparse = true;
					}
					markers.add(ex);
				}
			}
			catch (Exception ex)
			{
				// if (this.jumpBackToken != null)
				// {
				// tokens.jump(this.jumpBackToken);
				// this.popParser();
				// this.jumpBackToken = null;
				// }
				// else
				{
					DyvilCompiler.logger.throwing("ParserManager", "parseToken", ex);
					markers.add(new SyntaxError(token, "Failed to parse token '" + token + "': " + ex.getMessage()));
				}
			}
			
			if (this.parser == null)
			{
				break;
			}
			
			if (DyvilCompiler.parseStack)
			{
				System.out.println(token + ":\t\t" + this.parser.name + " @ " + this.parser.mode);
			}
		}
	}
	
	/**
	 * Returns true if the given {@link IToken} {@code token} should be parsed.
	 * If not, it gets removed from the Token Chain.
	 * 
	 * @param value
	 *            the value of the token
	 * @param token
	 *            the token
	 * @return true, if the token should be parser
	 */
	public boolean retainToken(IToken token, IToken prev)
	{
		if (!this.semicolonInference)
		{
			return true;
		}
		
		int type = token.type();
		if ((type & (Tokens.SYMBOL | Tokens.KEYWORD | Tokens.IDENTIFIER)) == 0)
		{
			return true;
		}
		
		if (prev == null)
		{
			return true;
		}
		
		int prevLN = prev.endLine();
		if (prevLN == token.startLine())
		{
			return true;
		}
		
		int type1 = prev.type();
		if (ParserUtil.isSeperator(type1))
		{
			return true;
		}
		
		int prevEnd = prev.endIndex();
		IToken semicolon = new InferredSemicolon(prevLN, prevEnd);
		semicolon.setNext(token);
		prev.setNext(semicolon);
		return true;
	}
	
	@Override
	public void skip()
	{
		this.skip++;
	}
	
	@Override
	public void skip(int tokens)
	{
		this.skip += tokens;
	}
	
	@Override
	public void reparse()
	{
		this.reparse = true;
	}
	
	@Override
	public void setParser(Parser parser)
	{
		this.parser = parser;
	}
	
	@Override
	public Parser getParser()
	{
		return this.parser;
	}
	
	@Override
	public void pushParser(Parser parser)
	{
		parser.parent = this.parser;
		this.parser = parser;
	}
	
	@Override
	public void pushParser(Parser parser, boolean reparse)
	{
		parser.parent = this.parser;
		this.parser = parser;
		this.reparse = reparse;
	}
	
	@Override
	public void popParser()
	{
		// Drop the jumpback token since the tryparser has completed
		// successfully.
		// this.jumpBackToken = null;
		this.parser = this.parser.parent;
	}
	
	@Override
	public void popParser(boolean reparse)
	{
		// Drop the jumpback token since the tryparser has completed
		// successfully.
		// this.jumpBackToken = null;
		this.parser = this.parser.parent;
		this.reparse = reparse;
	}
}

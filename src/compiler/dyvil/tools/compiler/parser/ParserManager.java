package dyvil.tools.compiler.parser;

import java.util.List;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.lexer.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
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
		this(Parser.rootParser);
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
	
	public final void parse(List<Marker> markers, TokenIterator tokens)
	{
		IToken token = null;
		this.tokens = tokens;
		
		try
		{
			IToken prev = null;
			while (tokens.hasNext())
			{
				token = tokens.next();
				if (!this.retainToken((Token) token, prev))
				{
					tokens.remove();
				}
				prev = token;
			}
			
			int index = 0;
			tokens.reset();
			while (tokens.hasNext())
			{
				token = tokens.next();
				token.setIndex(index);
				token.setPrev(prev);
				index++;
				prev = token;
			}
			
			tokens.reset();
			while (tokens.hasNext())
			{
				if (this.reparse)
				{
					this.reparse = false;
				}
				else
				{
					token = tokens.next();
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
						markers.add(new SyntaxError(token, "Failed to parse token '" + token.getText() + "': " + ex.getMessage()));
					}
				}
				
				if (DyvilCompiler.parseStack)
				{
					System.out.println(token + ":\t\t" + this.parser.name + " @ " + this.parser.mode);
				}
			}
		}
		catch (Exception ex)
		{
			DyvilCompiler.logger.throwing("ParserManager", "parse", ex);
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
	public boolean retainToken(Token token, IToken prev)
	{
		if (!this.semicolonInference)
		{
			return true;
		}
		
		try
		{
			int type = token.type();
			if (!ParserUtil.isIdentifier(type) && (type & Tokens.TYPE_KEYWORD) == 0)
			{
				return true;
			}
			
			if (prev == null)
			{
				return true;
			}
			
			int prevLN = prev.getLineNumber();
			if (prevLN == token.getLineNumber())
			{
				return true;
			}
			
			int type1 = prev.type();
			if (ParserUtil.isSeperator(type1))
			{
				return true;
			}
			
			int prevEnd = prev.getEnd();
			Token semicolon = new Token(0, ";", Tokens.SEMICOLON, ";", prev.getFile(), prevLN, prevEnd, prevEnd + 1);
			semicolon.setNext(token);
			prev.setNext(semicolon);
		}
		catch (SyntaxError error)
		{
		}
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

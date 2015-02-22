package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class ParserManager
{
	protected Parser		currentParser;
	
	public CodeFile			file;
	public boolean			semicolonInference;
	
	protected TokenIterator	tokens;
	protected IToken		currentToken;
	protected IToken		jumpBackToken;
	protected int			skip;
	
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
		this.currentParser = parser;
		this.currentParser.begin(this);
	}
	
	public final void parse(CodeFile file, Parser parser)
	{
		this.currentParser = parser;
		parser.begin(this);
		Dlex lexer = new Dlex(file);
		lexer.tokenize();
		this.parse(file, lexer.iterator());
	}
	
	public final void parse(CodeFile file, TokenIterator tokens)
	{
		IToken token = null;
		this.tokens = tokens;
		this.file = file;
		
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
				token = this.currentToken = tokens.next();
				
				if (this.skip > 0)
				{
					this.skip--;
					continue;
				}
				
				Parser parser = this.currentParser;
				try
				{
					parser.parse(this, token);
				}
				catch (SyntaxError ex)
				{
					if (this.jumpBackToken != null)
					{
						tokens.jump(this.jumpBackToken);
						this.popParser();
						this.jumpBackToken = null;
					}
					else
					{
						if (ex.reparse)
						{
							this.tokens.jump(this.currentToken);
						}
						this.file.markers.add(ex);
					}
				}
				catch (Exception ex)
				{
					if (this.jumpBackToken != null)
					{
						tokens.jump(this.jumpBackToken);
						this.popParser();
						this.jumpBackToken = null;
					}
					else
					{
						DyvilCompiler.logger.throwing("ParserManager", "parseToken", ex);
						this.file.markers.add(new SyntaxError(token, "Failed to parse token '" + token.getText() + "': " + ex.getMessage()));
					}
				}
				
				if (DyvilCompiler.parseStack)
				{
					System.out.println(token + ":\t\t" + parser.name + " @ " + parser.mode);
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
			Token semicolon = new Token(0, ";", Tokens.SEMICOLON, ";", this.file, prevLN, prevEnd, prevEnd + 1);
			semicolon.setNext(token);
			prev.setNext(semicolon);
		}
		catch (SyntaxError error)
		{
		}
		return true;
	}
	
	public void skip()
	{
		this.skip++;
	}
	
	public void skip(int tokens)
	{
		this.skip += tokens;
	}
	
	public void pushTryParser(Parser parser, IToken token)
	{
		this.jumpBackToken = token;
		this.pushParser(parser, false);
	}
	
	public void pushTryParser(Parser parser, IToken token, boolean reparse) throws SyntaxError
	{
		this.jumpBackToken = token;
		this.pushParser(parser, reparse);
	}
	
	public void setParser(Parser parser)
	{
		this.currentParser = parser;
		parser.begin(this);
	}
	
	public void pushParser(Parser parser)
	{
		if (this.currentParser != null)
		{
			parser.setParent(this.currentParser);
		}
		this.currentParser = parser;
		parser.begin(this);
	}
	
	public void pushParser(Parser parser, boolean reparse)
	{
		if (this.currentParser != null)
		{
			parser.setParent(this.currentParser);
		}
		this.currentParser = parser;
		parser.begin(this);
		if (reparse)
		{
			this.tokens.jump(this.currentToken);
		}
	}
	
	public void popParser() throws SyntaxError
	{
		// Drop the jumpback token since the tryparser has completed
		// successfully.
		this.jumpBackToken = null;
		if (this.currentParser != null)
		{
			this.currentParser.end(this);
			this.currentParser = this.currentParser.getParent();
		}
	}
	
	public void popParser(boolean reparse) throws SyntaxError
	{
		// Drop the jumpback token since the tryparser has completed
		// successfully.
		this.jumpBackToken = null;
		if (this.currentParser != null)
		{
			this.currentParser.end(this);
			this.currentParser = this.currentParser.getParent();
		}
		
		if (reparse)
		{
			this.tokens.jump(this.currentToken);
		}
	}
	
	public void reparse()
	{
		this.tokens.jump(this.currentToken);
	}
}

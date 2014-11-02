package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.Dyvilc;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public class ParserManager
{
	protected Parser		currentParser;
	
	public CodeFile			file;
	
	protected TokenIterator	tokens;
	protected IToken		currentToken;
	protected IToken		jumpBackToken;
	
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
		TokenIterator tokens = this.tokenize(file);
		this.parse(file, tokens);
	}
	
	public final TokenIterator tokenize(CodeFile file)
	{
		this.file = file;
		
		Dlex lexer = new Dlex(file);
		lexer.tokenize();
		return lexer.iterator();
	}
	
	public final void parse(CodeFile file, TokenIterator tokens)
	{
		IToken token = null;
		this.tokens = tokens;
		
		try
		{
			boolean removed = false;
			
			while (tokens.hasNext())
			{
				token = tokens.next();
				if (!this.retainToken(token.getText(), token))
				{
					tokens.remove();
					removed = true;
				}
			}
			
			if (removed)
			{
				int index = 0;
				
				tokens.reset();
				while (tokens.hasNext())
				{
					token = tokens.next();
					token.setIndex(index);
					index++;
				}
			}
			
			tokens.reset();
			while (tokens.hasNext())
			{
				Parser parser = this.currentParser;
				token = this.currentToken = tokens.next();
				try
				{
					this.parseToken(parser, token);
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
						this.file.markers.add(ex);
					}
				}
				
				if (Dyvilc.parseStack)
				{
					System.out.println(token + ":\t\t" + parser.name + " @ " + parser.mode);
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
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
	public boolean retainToken(String value, IToken token)
	{
		return true;
	}
	
	protected void parseToken(Parser parser, IToken token) throws SyntaxError
	{
		String value = token.value();
		boolean parsed;
		try
		{
			parsed = parser.parse(this, value, token);
		}
		catch (SyntaxError error)
		{
			throw error;
		}
		catch (Exception ex)
		{
			String message = ex.getMessage();
			if (message == null)
			{
				message = ex.getClass().getName();
			}
			ex.printStackTrace();
			throw new SyntaxError(token, "Failed to parse token '" + value + "': " + message);
		}
		
		if (!parsed)
		{
			throw new SyntaxError(token, "Invalid token '" + value + "'", "Delete this token");
		}
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
}

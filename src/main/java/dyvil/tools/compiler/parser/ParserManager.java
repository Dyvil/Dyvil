package dyvil.tools.compiler.parser;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.lexer.Dlex;
import dyvil.tools.compiler.lexer.Dlex.TokenIterator;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public class ParserManager
{
	protected Parser	currentParser;
	
	public CodeFile		file;
	public List<Marker>	markers;
	
	protected IToken	lastToken;
	
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
		this.parse(file);
	}
	
	public void parse(CodeFile file)
	{
		this.file = file;
		this.markers = new ArrayList();
		
		Dlex lexer = new Dlex(file);
		lexer.tokenize();
		this.parse(file, lexer);
		
		if (!this.markers.isEmpty())
		{
			System.err.println("Markers in File " + file.getName());
			
			for (Marker marker : this.markers)
			{
				marker.print(System.err);
			}
		}
	}
	
	public final void parse(CodeFile file, Dlex lexer)
	{
		IToken token = null;
		
		try
		{
			TokenIterator iterator = lexer.iterator();
			boolean removed = false;
			
			while (iterator.hasNext())
			{
				token = iterator.next();
				if (!this.retainToken(token.getText(), token))
				{
					iterator.remove();
					removed = true;
				}
			}
			
			if (removed)
			{
				int index = 0;
				
				iterator.reset();
				while (iterator.hasNext())
				{
					token = iterator.next();
					token.setIndex(index);
					index++;
				}
			}
			
			iterator.reset();
			while (iterator.hasNext())
			{
				try
				{
					token = iterator.next();
					this.parseToken(this.currentParser, token);
				}
				catch (SyntaxError ex)
				{
					if (this.lastToken != null)
					{
						iterator.jump(this.lastToken);
						this.popParser();
						this.lastToken = null;
					}
					else
					{
						this.markers.add(ex);
					}
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
			throw new SyntaxError(token, "Failed to parse token '" + value + "': " + message);
		}
		
		if (!parsed)
		{
			throw new SyntaxError(token, "Invalid token '" + value + "'", "Delete this token");
		}
	}
	
	public void tryParse(Parser parser, IToken start) throws SyntaxError
	{
		this.lastToken = start;
		this.pushParser(parser, start);
	}
	
	/**
	 * Adds the given {@link Parser} {@code parser} to the stack.
	 * 
	 * @see Parser#parse(ParserManager, String, IToken)
	 * @param parser
	 *            the parser
	 * @throws SyntaxError
	 *             syntax errors
	 */
	public void pushParser(Parser parser)
	{
		if (this.currentParser != null)
		{
			parser.setParent(this.currentParser);
		}
		this.currentParser = parser;
		parser.begin(this);
	}
	
	/**
	 * Adds the given {@link Parser} {@code parser} to the stack and makes it
	 * parse the given {@link IToken} {@code token}.
	 * 
	 * @see #pushParser(Parser)
	 * @see Parser#parse(ParserManager, String, IToken)
	 * @param parser
	 *            the parser
	 * @throws SyntaxError
	 *             syntax errors
	 */
	public void pushParser(Parser parser, IToken token) throws SyntaxError
	{
		this.pushParser(parser);
		this.parseToken(parser, token);
	}
	
	public void popParser()
	{
		if (this.currentParser != null)
		{
			this.currentParser.end(this);
			this.currentParser = this.currentParser.getParent();
		}
	}
	
	public void popParser(IToken token) throws SyntaxError
	{
		this.popParser();
		if (this.currentParser != null)
		{
			this.parseToken(this.currentParser, token);
		}
	}
}

package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public abstract class Parser<T>
{
	public static final Parser	rootParser	= new Parser()
											{
												@Override
												public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
												{
													return false;
												}
											};
	
	private Parser				parent;
	protected int				mode;
	
	public Parser()
	{
		this.parent = rootParser;
	}
	
	public Parser(Parser parent)
	{
		this.parent = parent;
	}
	
	public Parser getParent()
	{
		return this.parent;
	}
	
	public void setParent(Parser parent)
	{
		if (parent != null)
		{
			this.parent = parent;
		}
	}
	
	public boolean isInMode(int mode)
	{
		if (mode == 0)
		{
			return this.mode == 0;
		}
		return (this.mode & mode) == mode;
	}
	
	public void begin(ParserManager pm)
	{
	}
	
	public abstract boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError;
	
	public void end(ParserManager pm)
	{
	}
}

package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.DyvilCompiler;
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
	
	protected int				mode;
	
	public Parser				parent;
	public String				name;
	
	public Parser()
	{
		this.parent = rootParser;
		if (DyvilCompiler.parseStack)
		{
			this.name = this.computeName();
		}
	}
	
	public Parser(Parser parent)
	{
		this.parent = parent;
		if (DyvilCompiler.parseStack)
		{
			this.name = parent.name + "." + this.computeName();
		}
	}
	
	public Parser getParent()
	{
		return this.parent;
	}
	
	protected String computeName()
	{
		String s = this.getClass().getSimpleName();
		int index = s.indexOf("Parser");
		if (index != -1)
		{
			s = s.substring(0, index);
		}
		return s.toLowerCase();
	}
	
	public void setParent(Parser parent)
	{
		if (parent != null)
		{
			this.parent = parent;
			if (DyvilCompiler.parseStack)
			{
				this.name = parent.name + "." + this.computeName();
			}
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
	{}
	
	public abstract boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError;
	
	public void end(ParserManager pm)
	{}
}

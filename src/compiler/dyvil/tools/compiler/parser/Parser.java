package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;

public abstract class Parser
{
	public static final Parser	rootParser	= new Parser()
											{
												
												@Override
												public void reset()
												{
												};
												
												@Override
												public void parse(IParserManager pm, IToken token) throws SyntaxError
												{
													throw new SyntaxError(token, "Root Parser");
												}
											};
	
	protected int				mode;
	
	protected Parser			parent;
	protected String			name;
	
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
	
	public final boolean isInMode(int mode)
	{
		if (mode == 0)
		{
			return this.mode == 0;
		}
		return (this.mode & mode) == mode;
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
	
	public Parser getParent()
	{
		return this.parent;
	}
	
	public abstract void reset();
	
	public abstract void parse(IParserManager pm, IToken token) throws SyntaxError;
}

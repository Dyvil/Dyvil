package dyvil.tools.compiler.parser;

import dyvil.tools.parsing.token.IToken;

public abstract class Parser
{
	public static final Parser rootParser = new Parser()
	{
		@Override
		public void parse(IParserManager pm, IToken token)
		{
			if (!ParserUtil.isTerminator(token.type()))
			{
				pm.report(token, "Root Parser");
				return;
			}
		}
	};
	
	protected static final int END = -1;
	
	protected int mode;
	
	protected Parser parent;
	
	public Parser()
	{
	}
	
	public Parser(Parser parent)
	{
		this.parent = parent;
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
	
	public int getMode()
	{
		return this.mode;
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
		}
	}
	
	public Parser getParent()
	{
		return this.parent;
	}
	
	public abstract void parse(IParserManager pm, IToken token);

	public boolean reportErrors()
	{
		return false;
	}
}

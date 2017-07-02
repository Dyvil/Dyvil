package dyvil.tools.parsing;

import dyvil.tools.parsing.token.IToken;

public abstract class Parser
{
	/**
	 * The END state, shared by all Parser subclasses for convenience.
	 */
	protected static final int END = -1;

	/**
	 * The current Parser state.
	 */
	protected int mode;

	/**
	 * The parent Parser, which will be used be used when this Parser {@code POP}s from the Parser stack.
	 */
	protected Parser parent;

	public Parser()
	{
	}

	public Parser(Parser parent)
	{
		this.parent = parent;
	}

	public int getMode()
	{
		return this.mode;
	}

	public void setMode(int mode)
	{
		this.mode = mode;
	}

	public Parser getParent()
	{
		return this.parent;
	}

	public void setParent(Parser parent)
	{
		this.parent = parent;
	}

	/**
	 * Parses the given {@code token} according to this Parser's rules and state.
	 *
	 * @param pm
	 * 	the current parsing context manager
	 * @param token
	 * 	the token to parser
	 */
	public abstract void parse(IParserManager pm, IToken token);

	/**
	 * Returns {@code true} if errors should be reported for this Parser, {@code false} if they should be ignored. This
	 * is used by the REPL parser to determine how to parse a certain input.
	 *
	 * @return {@code true} if errors should be reported for this Parser, {@code false} if they should be ignored.
	 */
	public boolean reportErrors()
	{
		return true;
	}
}

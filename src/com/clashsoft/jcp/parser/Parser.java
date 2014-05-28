package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.JCPHelper;
import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;

public abstract class Parser
{
	private Parser	parent;
	protected int	modifiers;
	
	public Parser()
	{
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
		this.parent = parent;
	}
	
	public int addModifier(int mod)
	{
		this.modifiers |= mod;
		return this.modifiers;
	}
	
	public int removeModifier(int mod)
	{
		this.modifiers &= ~mod;
		return this.modifiers;
	}
	
	public boolean checkModifier(String token)
	{
		int mod = JCPHelper.parseModifier(token);
		if (mod != 0)
		{
			this.addModifier(mod);
			return true;
		}
		return false;
	}
	
	public final void parse(JCP jcp, String code)
	{
		// Create a list of raw tokens
		Token token = JCPHelper.tokenize(code);
		while (token.next() != null)
		{
			try
			{
				jcp.parser.parse(jcp, token.value, token);
			}
			catch (SyntaxException ex)
			{
				ex.print(System.err, code, token);
			}
			token = token.next();
		}
	}
	
	public void begin(JCP jcp) throws SyntaxException
	{
	}
	
	public abstract void parse(JCP jcp, String value, Token token) throws SyntaxException;
	
	public void end(JCP jcp) throws SyntaxException
	{
	}
}

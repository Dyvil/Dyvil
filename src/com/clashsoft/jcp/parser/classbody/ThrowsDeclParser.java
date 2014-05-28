package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.member.methods.IThrower;
import com.clashsoft.jcp.ast.member.methods.ThrowsDecl;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

public class ThrowsDeclParser extends Parser
{	
	private IThrower thrower;
	
	private String exception;
	
	public ThrowsDeclParser(IThrower thrower)
	{
		this.thrower = thrower;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (",".equals(value))
		{
			if (this.exception == null)
			{
				throw new SyntaxException("throwsdecl.invalid.comma");
			}
			this.thrower.addThrowsDecl(new ThrowsDecl(this.exception));
			this.exception = null;
		}
		else
		{
			this.exception = value;
		}
	}
}

package com.clashsoft.jcp.ast.member.methods;

import com.clashsoft.jcp.SyntaxException;

public class ThrowsDecl
{
	private String exception;
	
	public ThrowsDecl(String exception)
	{
		
	}
	
	public void setException(String exception) throws SyntaxException
	{
		if (this.exception != null)
		{
			throw new SyntaxException("throwsdecl.exception.set", exception);
		}
		this.exception = exception;
	}
	
	public String getException()
	{
		return this.exception;
	}
}

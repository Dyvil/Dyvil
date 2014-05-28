package com.clashsoft.jcp.parser.classbody;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.member.Implementation;
import com.clashsoft.jcp.ast.member.methods.IImplementable;
import com.clashsoft.jcp.parser.JCP;
import com.clashsoft.jcp.parser.Parser;

public class ImplementationParser extends Parser
{
	private IImplementable implementable;
	
	public ImplementationParser(IImplementable implementable)
	{
		this.implementable = implementable;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		Implementation impl = new Implementation();
		
		if ("}".equals(value))
		{
			jcp.popParser();
		}
		
		this.implementable.setImplementation(impl);
	}
}

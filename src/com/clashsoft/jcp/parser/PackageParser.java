package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.PackageDecl;

public class PackageParser extends Parser
{
	public StringBuilder	buffer	= new StringBuilder();
	public PackageDecl		packageDecl;
	
	public PackageParser(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (";".equals(value))
		{
			jcp.popParser();
		}
		else
		{
			this.buffer.append(value);
		}
	}
	
	@Override
	public void end(JCP jcp) throws SyntaxException
	{
		this.packageDecl.setPackage(this.buffer.toString());
	}
}

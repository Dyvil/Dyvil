package com.clashsoft.jcp.ast;

import com.clashsoft.jcp.SyntaxException;

public class PackageDecl
{
	private String thePackage;
	
	public void setPackage(String s) throws SyntaxException
	{
		this.thePackage = s;
	}
	
	public String getPackage()
	{
		return this.thePackage;
	}
}

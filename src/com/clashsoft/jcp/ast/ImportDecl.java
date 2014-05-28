package com.clashsoft.jcp.ast;

import com.clashsoft.jcp.SyntaxException;

public class ImportDecl
{
	public static final int PHASE_INIT = 0;
	public static final int PHASE_PACKAGE = 1;
	public static final int PHASE_ASTERISK = 2;
	
	private String			theImport;
	
	private boolean			isStatic;
	private boolean			asterisk;
	
	public void setImport(String theImport) throws SyntaxException
	{
		this.theImport = theImport;
	}
	
	public void setStatic() throws SyntaxException
	{
		this.isStatic = true;
	}
	
	public void setAsterisk() throws SyntaxException
	{
		this.asterisk = true;
	}
	
	public String getImport()
	{
		return this.theImport;
	}
	
	public boolean isAsterisk()
	{
		return this.asterisk;
	}
	
	public boolean isStatic()
	{
		return this.isStatic;
	}
}

package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.ImportDecl;

public class ImportParser extends Parser
{
	public static final int	PACKAGE		= 1;
	public static final int	ASTERISK	= 2;
	
	private int				mode;
	private StringBuilder	buffer		= new StringBuilder();
	
	private ImportDecl		importDecl;
	
	public ImportParser(ImportDecl importDecl)
	{
		this.importDecl = importDecl;
	}
	
	@Override
	public void parse(JCP jcp, String value, Token token) throws SyntaxException
	{
		if (";".equals(value))
		{
			jcp.popParser();
		}
		else if ("static".equals(value))
		{
			if (this.mode == 0)
			{
				this.importDecl.setStatic();
			}
			else
			{
				throw new SyntaxException("import.static.invalid");
			}
		}
		else if ("*".equals(value))
		{
			if (this.mode == PACKAGE)
			{
				this.mode = ASTERISK;
				this.importDecl.setAsterisk();
			}
			else
			{
				throw new SyntaxException("import.asterisk.invalid");
			}
		}
		else
		{
			if (this.mode == ASTERISK)
			{
				throw new SyntaxException("import.package.invalid");
			}
			this.buffer.append(value);
		}
	}
	
	@Override
	public void end(JCP jcp) throws SyntaxException
	{
		this.importDecl.setImport(this.buffer.toString());
	}
}

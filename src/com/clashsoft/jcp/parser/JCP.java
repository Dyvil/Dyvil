package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.JCPHelper;
import com.clashsoft.jcp.SyntaxException;
import com.clashsoft.jcp.Token;
import com.clashsoft.jcp.ast.CompilationUnit;

public class JCP
{
	public static final int		COMPILATION_UNIT	= 1;
	
	protected Parser			parser;
	protected int				mode;
	
	public CompilationUnit compilationUnit(String code) throws SyntaxException
	{
		this.mode = COMPILATION_UNIT;
		
		CompilationUnit unit = new CompilationUnit();
		this.parse(new CompilationUnitParser(unit), code);
		
		return unit;
	}
	
	public final void parse(String code)
	{
		// Create a list of raw tokens
		Token token = JCPHelper.tokenize(code);
		while (token.next() != null)
		{
			try
			{
				this.parser.parse(this, token.value, token);
			}
			catch (SyntaxException ex)
			{
				ex.print(System.err, code, token);
			}
			token = token.next();
		}
	}
	
	public void parse(Parser parser, String code) throws SyntaxException
	{
		this.parser = parser;
		parser.begin(this);
		this.parse(code);
	}
	
	public void pushParser(Parser parser) throws SyntaxException
	{
		if (this.parser != null)
		{
			parser.setParent(this.parser);
		}
		this.parser = parser;
		parser.begin(this);
	}
	
	public void pushParser(Parser parser, Token token) throws SyntaxException
	{
		this.pushParser(parser);
		parser.parse(this, token.value, token);
	}
	
	public Parser popParser() throws SyntaxException
	{
		if (this.parser != null)
		{
			Parser parser = this.parser;
			parser.end(this);
			this.parser = parser.getParent();
			return parser;
		}
		return null;
	}
}

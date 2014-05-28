package com.clashsoft.jcp.parser;

import com.clashsoft.jcp.SyntaxException;
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
	
	public void parse(Parser parser, String code) throws SyntaxException
	{
		this.parser = parser;
		parser.begin(this);
		parser.parse(this, code);
	}
	
	public void pushParser(Parser parser) throws SyntaxException
	{
		if (this.parser != null)
		{
			parser.setParent(this.parser);
		}
		parser.begin(this);
		this.parser = parser;
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

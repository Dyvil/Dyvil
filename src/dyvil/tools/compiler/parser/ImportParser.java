package dyvil.tools.compiler.parser;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.imports.SimpleImport;

public class ImportParser extends Parser
{
	private StringBuilder	buffer	= new StringBuilder();
	
	private SimpleImport	simpleImport;
	
	public ImportParser(SimpleImport simpleImport)
	{
		this.simpleImport = simpleImport;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
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
	public void end(ParserManager jcp)
	{
		this.simpleImport.setImport(this.buffer.toString());
	}
}

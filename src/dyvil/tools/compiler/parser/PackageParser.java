package dyvil.tools.compiler.parser;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.PackageDecl;

public class PackageParser extends Parser
{
	public StringBuilder	buffer	= new StringBuilder();
	public PackageDecl		packageDecl;
	
	public PackageParser(PackageDecl packageDecl)
	{
		this.packageDecl = packageDecl;
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
		this.packageDecl.setPackage(this.buffer.toString());
	}
}

package dyvil.tools.compiler.parser;

import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.ImportDecl;

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
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
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
	public void end(ParserManager jcp)
	{
		this.importDecl.setImport(this.buffer.toString());
	}
}

package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.classes.ClassDeclParser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;

public class CompilationUnitParser extends Parser
{
	private static final int	PACKAGE	= 0;
	private static final int	IMPORT	= 1;
	private static final int	CLASS	= 2;
	
	private CompilationUnit		unit;
	
	public CompilationUnitParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (this.isInMode(PACKAGE))
		{
			if ("package".equals(value))
			{
				this.mode = IMPORT | CLASS;
				jcp.pushParser(new PackageParser(this.unit));
				return true;
			}
			else if ("import".equals(value))
			{
				throw new SyntaxError("Missing package declaration!", "Add a package declaration");
			}
		}
		if (this.isInMode(IMPORT))
		{
			if ("import".equals(value))
			{
				jcp.pushParser(new ImportParser(this.unit));
				return true;
			}
		}
		if (this.isInMode(CLASS))
		{
			jcp.pushParser(new ClassDeclParser(this.unit), token);
			return true;
		}
		return false;
	}
}

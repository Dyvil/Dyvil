package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.classes.ClassDeclParser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;

public class CompilationUnitParser extends Parser
{
	private static final int	PACKAGE	= 1;
	private static final int	IMPORT	= 2;
	private static final int	CLASS	= 4;
	
	private CompilationUnit		unit;
	
	public CompilationUnitParser(CompilationUnit unit)
	{
		this.unit = unit;
		this.mode = PACKAGE | IMPORT | CLASS;
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
				this.mode = IMPORT | CLASS;
				throw new SyntaxError(token, "Missing package declaration!", "Add a package declaration");
			}
		}
		if (this.isInMode(IMPORT))
		{
			if ("import".equals(value))
			{
				this.mode = IMPORT | CLASS;
				jcp.pushParser(new ImportParser(null, this.unit));
				return true;
			}
		}
		if (this.isInMode(CLASS))
		{
			jcp.pushParser(new ClassDeclParser(this.unit), true);
			return true;
		}
		return false;
	}
}

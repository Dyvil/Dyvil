package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.util.Tokens;

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
	public boolean parse(ParserManager jcp, IToken token) throws SyntaxError
	{
		String value = token.value();
		if (this.isInMode(PACKAGE))
		{
			if ("package".equals(value))
			{
				this.mode = IMPORT | CLASS;
				jcp.pushParser(new PackageParser(this.unit));
				return true;
			}
		}
		if (this.isInMode(IMPORT))
		{
			if ("import".equals(value))
			{
				this.mode = IMPORT | CLASS;
				Import i = new Import(token.raw());
				this.unit.addImport(i);
				jcp.pushParser(new ImportParser(null, i));
				return true;
			}
			if ("using".equals(value))
			{
				this.mode = IMPORT | CLASS;
				Import i = new Import(token.raw());
				i.isStatic = true;
				this.unit.addStaticImport(i);
				jcp.pushParser(new ImportParser(null, i));
				return true;
			}
		}
		if (this.isInMode(CLASS))
		{
			if (token.isType(Tokens.SEMICOLON))
			{
				return true;
			}
			
			jcp.pushParser(new ClassDeclParser(this.unit), true);
			return true;
		}
		return false;
	}
	
}

package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.classes.CodeClass;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.structure.IDyvilUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.util.Tokens;

public class CompilationUnitParser extends Parser
{
	private static final int	PACKAGE	= 1;
	private static final int	IMPORT	= 2;
	private static final int	CLASS	= 4;
	
	protected IDyvilUnit		unit;
	
	public CompilationUnitParser(IDyvilUnit unit)
	{
		this.unit = unit;
		this.mode = PACKAGE | IMPORT | CLASS;
	}
	
	@Override
	public void reset()
	{
		this.mode = PACKAGE | IMPORT | CLASS;
	}
	
	@Override
	public void parse(IParserManager jcp, IToken token) throws SyntaxError
	{
		String value = token.value();
		if (this.isInMode(PACKAGE))
		{
			if ("package".equals(value))
			{
				this.mode = IMPORT | CLASS;
				
				PackageDecl pack = new PackageDecl(token.raw());
				this.unit.setPackageDeclaration(pack);
				jcp.pushParser(new PackageParser(pack));
				return;
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
				return;
			}
			if ("using".equals(value))
			{
				this.mode = IMPORT | CLASS;
				Import i = new Import(token.raw());
				i.isStatic = true;
				this.unit.addStaticImport(i);
				jcp.pushParser(new ImportParser(null, i));
				return;
			}
		}
		if (this.isInMode(CLASS))
		{
			if (token.type() == Tokens.SEMICOLON)
			{
				return;
			}
			
			CodeClass c = new CodeClass(null, this.unit);
			this.unit.addClass(c);
			jcp.pushParser(new ClassDeclParser(c), true);
			return;
		}
		throw new SyntaxError(token, "Invalid Token - Delete this token");
	}
}

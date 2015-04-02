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
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;

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
		int type = token.type();
		if (this.isInMode(PACKAGE))
		{
			if (type == Keywords.PACKAGE)
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
			if (type == Keywords.IMPORT)
			{
				this.mode = IMPORT | CLASS;
				Import i = new Import(token.raw());
				this.unit.addImport(i);
				jcp.pushParser(new ImportParser(i));
				return;
			}
			if (type == Keywords.USING)
			{
				this.mode = IMPORT | CLASS;
				Import i = new Import(token.raw(), true);
				this.unit.addStaticImport(i);
				jcp.pushParser(new ImportParser(i));
				return;
			}
		}
		if (this.isInMode(CLASS))
		{
			if (token.type() == Symbols.SEMICOLON)
			{
				return;
			}
			
			CodeClass c = new CodeClass(null, this.unit);
			this.unit.addClass(c);
			jcp.pushParser(new ClassDeclarationParser(c), true);
			return;
		}
		throw new SyntaxError(token, "Invalid Token - Delete this token");
	}
}

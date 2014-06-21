package dyvil.tools.compiler.parser;

import clashsoft.cslib.src.CSSource;
import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.ClassDecl;
import dyvil.tools.compiler.ast.CompilationUnit;

public class CompilationUnitParser extends Parser
{
	private CompilationUnit unit;
	
	public CompilationUnitParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public void parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		int mod;
		
		if ("package".equals(value))
		{
			jcp.pushParser(new PackageParser(this.unit));
			return;
		}
		else if ("import".equals(value))
		{
			jcp.pushParser(new ImportParser(this.unit));
			return;
		}
		else if (CSSource.isClass(value))
		{
			ClassDecl classDecl = new ClassDecl();
			this.unit.setClassDecl(classDecl);
			jcp.pushParser(new ClassDeclParser(classDecl));
		}
		else
		{
			this.checkModifier(value);
		}
	}
}

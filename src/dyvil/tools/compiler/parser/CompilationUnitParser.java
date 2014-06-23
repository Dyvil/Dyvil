package dyvil.tools.compiler.parser;

import clashsoft.cslib.src.CSSource;
import clashsoft.cslib.src.SyntaxException;
import clashsoft.cslib.src.parser.IToken;
import clashsoft.cslib.src.parser.Parser;
import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.parser.classes.ClassDeclParser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;

public class CompilationUnitParser extends Parser
{
	private CompilationUnit unit;
	
	public CompilationUnitParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	/**
	 * Expands the class name from the simple name to the full name, with package.<p>
	 * Example:
	 * name = "String" -> "java.lang.String"
	 * name = "Random" -> "java.util.Random"
	 * 
	 * @param name
	 * @return
	 */
	public String resolveClass(String name)
	{
		return name;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxException
	{
		// TODO Modifiers
		if ("package".equals(value))
		{
			jcp.pushParser(new PackageParser(this.unit));
			return true;
		}
		else if ("import".equals(value))
		{
			jcp.pushParser(new ImportParser(this.unit));
			return true;
		}
		else if (CSSource.isClass(value))
		{
			jcp.pushParser(new ClassDeclParser(this.unit));
			return true;
		}
		return false;
	}
}

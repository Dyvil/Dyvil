package dyvil.tools.compiler.parser;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.lexer.SyntaxException;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.classes.ClassDeclParser;
import dyvil.tools.compiler.parser.imports.ImportParser;
import dyvil.tools.compiler.parser.imports.PackageParser;
import dyvil.tools.compiler.util.Classes;

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
		int i = 0;
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
		else if ((i = Classes.parse(value)) != -1)
		{
			AbstractClass c = AbstractClass.create(i);
			this.unit.addClass(c);
			jcp.pushParser(new ClassDeclParser(this.unit, c));
			return true;
		}
		return false;
	}
}

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
	private static final int	PACKAGE	= 0;
	private static final int	IMPORT	= 1;
	private static final int	CLASS	= 2;
	
	private CompilationUnit		unit;
	private int					modifiers;
	
	public CompilationUnitParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	/**
	 * Expands the class name from the simple name to the full name, with
	 * package.
	 * <p>
	 * Example: name = "String" -> "java.lang.String" name = "Random" ->
	 * "java.util.Random"
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
			if (this.mode != PACKAGE)
			{
				throw new SyntaxException("The package must be declared at the beginning of the class file.");
			}
			
			jcp.pushParser(new PackageParser(this.unit));
			this.mode = IMPORT;
			return true;
		}
		else if ("import".equals(value))
		{
			if (this.mode == PACKAGE)
			{
				throw new SyntaxException("Missing package declaration!");
			}
			else
			{
				this.mode = CLASS;
				jcp.pushParser(new ImportParser(this.unit));
				return true;
			}
		}
		else if ((i = Classes.parse(value)) != -1)
		{
			if (this.mode == PACKAGE)
			{
				throw new SyntaxException("Missing package declaration!");
			}
			else
			{
				AbstractClass c = AbstractClass.create(i);
				c.setModifiers(this.modifiers);
				this.modifiers = 0;
				this.unit.addClass(c);
				
				jcp.pushParser(new ClassDeclParser(this.unit, c));
				return true;
			}
		}
		return false;
	}
}

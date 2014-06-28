package dyvil.tools.compiler;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.parser.CompilationUnitParser;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class CodeParser extends ParserManager
{
	public static String	code	= "hello world;;;;; this is a \"test  ;;; hello world\" if this 'thing' works correctly";
	
	public static CompilationUnit unit;
	
	private CodeParser()
	{
		super();
	}
	
	private CodeParser(Parser parser)
	{
		super(parser);
	}
	
	public static CompilationUnit compilationUnit(String code)
	{
		unit = new CompilationUnit();
		new CodeParser(new CompilationUnitParser(unit)).parse(code);
		CompilationUnit cu = unit;
		unit = null;
		return cu;
	}
	
	public static AbstractClass resolveClass(String name)
	{
		// TODO Implement
		return null;
	}
}

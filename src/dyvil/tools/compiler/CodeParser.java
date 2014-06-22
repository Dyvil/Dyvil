package dyvil.tools.compiler;

import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.classes.AbstractClass;
import dyvil.tools.compiler.parser.CompilationUnitParser;

public class CodeParser extends ParserManager
{
	public static String	code	= "hello world;;;;; this is a \"test  ;;; hello world\" if this 'thing' works correctly";
	
	public static CompilationUnit unit;
	
	private CodeParser()
	{
	}
	
	public static CompilationUnit compilationUnit(String code)
	{
		unit = new CompilationUnit();
		CodeParser jcp = new CodeParser();
		jcp.currentParser = new CompilationUnitParser(unit);
		jcp.parse(code);
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

package dyvil.tools.compiler;

import clashsoft.cslib.src.parser.ParserManager;
import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.parser.CompilationUnitParser;

public class CodeParser extends ParserManager
{
	public static String	code	= "hello world;;;;; this is a \"test  ;;; hello world\" if this 'thing' works correctly";
	
	private CodeParser()
	{
	}
	
	public static CompilationUnit compilationUnit(String code)
	{
		CompilationUnit unit = new CompilationUnit();
		CodeParser jcp = new CodeParser();
		jcp.currentParser = new CompilationUnitParser(unit);
		jcp.parse(code);
		return unit;
	}
}

package dyvil.tools.compiler;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.parser.CompilationUnitParser;
import dyvil.tools.compiler.parser.ParserManager;

public class CodeParser extends ParserManager
{
	public static CodeParser	instance	= new CodeParser();
	
	private CodeParser()
	{
		super();
	}
	
	public static CompilationUnit compilationUnit(CodeFile file)
	{
		CompilationUnit unit = new CompilationUnit();
		instance.parse(file, new CompilationUnitParser(unit));
		return unit;
	}
}

package dyvil.tools.compiler;

import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.CodeFile;
import dyvil.tools.compiler.parser.CompilationUnitParser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.ast.structure.Package;

public class CodeParser extends ParserManager
{
	public static CodeParser	instance	= new CodeParser();
	
	private CodeParser()
	{
		super();
	}
	
	public static CompilationUnit compilationUnit(Package pack, CodeFile file)
	{
		CompilationUnit unit = new CompilationUnit(pack, file);
		instance.parse(file, new CompilationUnitParser(unit));
		unit.loadingTime = System.currentTimeMillis() - unit.loadingTime;
		return unit;
	}
}

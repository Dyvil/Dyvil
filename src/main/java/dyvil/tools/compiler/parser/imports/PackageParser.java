package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.lexer.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class PackageParser extends Parser
{
	protected CompilationUnit	unit;
	
	private StringBuilder		buffer	= new StringBuilder();
	
	public PackageParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value))
		{
			jcp.popParser();
			return true;
		}
		else if (token.isType(Token.TYPE_IDENTIFIER) || ".".equals(value))
		{
			this.buffer.append(value);
			return true;
		}
		return false;
	}
	
	@Override
	public void end(ParserManager jcp)
	{
		this.unit.setPackageDecl(new PackageDecl(this.buffer.toString()));
	}
}

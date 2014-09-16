package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.CompilationUnit;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.lexer.token.Token;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;

public class PackageParser extends Parser
{
	protected CompilationUnit	unit;
	
	private PackageDecl			packageDeclaration;
	private StringBuilder		buffer				= new StringBuilder();
	
	public PackageParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager jcp, String value, IToken token) throws SyntaxError
	{
		if (";".equals(value))
		{
			this.packageDeclaration.expandPosition(token);
			this.packageDeclaration.setPackage(this.buffer.toString());
			this.unit.setPackageDecl(this.packageDeclaration);
			
			jcp.popParser();
			return true;
		}
		else if (token.isType(Token.TYPE_IDENTIFIER) || ".".equals(value))
		{
			if (this.packageDeclaration == null)
			{
				this.packageDeclaration = new PackageDecl(token, null);
			}
			
			this.buffer.append(value);
			return true;
		}
		return false;
	}
}

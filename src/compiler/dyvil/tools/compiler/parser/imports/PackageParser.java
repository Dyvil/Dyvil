package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.structure.CompilationUnit;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class PackageParser extends Parser
{
	protected CompilationUnit	unit;
	
	private PackageDecl			packageDeclaration;
	private StringBuilder		buffer	= new StringBuilder();
	
	public PackageParser(CompilationUnit unit)
	{
		this.unit = unit;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			this.packageDeclaration.expandPosition(token);
			this.packageDeclaration.setPackage(this.buffer.toString());
			this.unit.setPackageDeclaration(this.packageDeclaration);
			
			pm.popParser();
			return true;
		}
		if (ParserUtil.isIdentifier(type) || type == Tokens.DOT)
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

package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.structure.DyvilFile;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class PackageParser extends Parser
{
	protected DyvilFile	unit;
	
	private PackageDecl			packageDeclaration;
	private StringBuilder		buffer	= new StringBuilder();
	
	public PackageParser(DyvilFile unit)
	{
		this.unit = unit;
	}
	
	@Override
	public void parse(ParserManager pm, IToken token) throws SyntaxError
	{
		int type = token.type();
		if (type == Tokens.SEMICOLON)
		{
			this.packageDeclaration.expandPosition(token);
			this.packageDeclaration.setPackage(this.buffer.toString());
			this.unit.setPackageDeclaration(this.packageDeclaration);
			
			pm.popParser();
			return;
		}
		if (ParserUtil.isIdentifier(type) || type == Tokens.DOT)
		{
			if (this.packageDeclaration == null)
			{
				this.packageDeclaration = new PackageDecl(token, null);
			}
			
			this.buffer.append(token.value());
			return;
		}
		throw new SyntaxError(token, "Invalid Package Declaration - Invalid Token '" + token.value() + "'");
	}
}

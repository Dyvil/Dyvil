package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.transform.Tokens;

public class PackageParser extends Parser
{
	protected PackageDeclaration	packageDeclaration;
	private StringBuilder			buffer	= new StringBuilder();
	
	public PackageParser(PackageDeclaration pack)
	{
		this.packageDeclaration = pack;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) 
	{
		int type = token.type();
		switch (type)
		{
		case Symbols.SEMICOLON:
			this.packageDeclaration.setPackage(this.buffer.toString());
			pm.popParser();
			return;
		case Keywords.TYPE:
			this.buffer.append("type");
			return;
		case Keywords.ANNOTATION:
			this.buffer.append("annotation");
			return;
		case Symbols.DOT:
			this.buffer.append('.');
			return;
		case Tokens.IDENTIFIER:
		case Tokens.LETTER_IDENTIFIER:
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.DOT_IDENTIFIER:
			this.buffer.append(token.nameValue().qualified);
			return;
		}
		pm.report(new SyntaxError(token, "Invalid Package Declaration - Invalid " + token)); return;
	}
}

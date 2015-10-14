package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.PackageDeclaration;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

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
		case BaseSymbols.SEMICOLON:
			this.packageDeclaration.setPackage(this.buffer.toString());
			pm.popParser();
			return;
		case BaseSymbols.DOT:
			this.buffer.append('.');
			return;
		case Tokens.IDENTIFIER:
		case Tokens.LETTER_IDENTIFIER:
		case Tokens.SYMBOL_IDENTIFIER:
		case Tokens.SPECIAL_IDENTIFIER:
			this.buffer.append(token.nameValue().qualified);
			return;
		}
		pm.report(token, "Invalid Package Declaration - Invalid " + token);
		return;
	}
}

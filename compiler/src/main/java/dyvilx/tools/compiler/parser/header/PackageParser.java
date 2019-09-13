package dyvilx.tools.compiler.parser.header;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.header.PackageDeclaration;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class PackageParser extends Parser
{
	private static final int PACKAGE = 0;
	private static final int NAME    = 1;
	private static final int DOT     = 2;

	protected PackageDeclaration packageDeclaration;
	private StringBuilder buffer = new StringBuilder();

	public PackageParser(PackageDeclaration pack)
	{
		this.packageDeclaration = pack;
		this.mode = NAME;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case PACKAGE:
			this.mode = NAME;
			if (type != DyvilKeywords.PACKAGE)
			{
				pm.report(token, "package.package");
				pm.reparse();
			}
			return;
		case NAME:
			if (Tokens.isIdentifier(type))
			{
				final Name name = token.nameValue();
				this.buffer.append(name.qualified);
				this.mode = DOT;
				return;
			}
			pm.report(token, "package.identifier");
			return;
		case DOT:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				pm.reparse();
				// Fallthrough
			case Tokens.EOF:
				this.packageDeclaration.setPackage(this.buffer.toString());
				pm.popParser();
				return;
			case BaseSymbols.DOT:
				this.mode = NAME;
				this.buffer.append('.');
				return;
			default:
				this.mode = NAME;
				pm.report(token, "package.dot");
				pm.reparse();
				return;
			}
		}
	}
}

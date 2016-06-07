package dyvil.tools.compiler.parser.header;

import dyvil.tools.compiler.ast.header.PackageDeclaration;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

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
			if (ParserUtil.isIdentifier(type))
			{
				final Name name = token.nameValue();
				this.buffer.append(name.qualified);
				this.mode = DOT;
				return;
			}
			pm.report(token, "package.identifier");
			return;
		case DOT:
			if (type == BaseSymbols.SEMICOLON)
			{
				this.packageDeclaration.setPackage(this.buffer.toString());
				pm.popParser();
				return;
			}
			this.mode = NAME;
			if (type == BaseSymbols.DOT)
			{
				this.buffer.append('.');
			}
			else
			{
				pm.report(token, "package.dot");
				pm.reparse();
			}
		}
	}

	@Override
	public boolean reportErrors()
	{
		return true;
	}
}

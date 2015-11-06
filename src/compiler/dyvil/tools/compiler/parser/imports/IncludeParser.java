package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class IncludeParser extends Parser
{
	private static final int	INCLUDE	= 0;
	private static final int	NAME	= 1;
	private static final int	DOT		= 2;
	
	private IDyvilHeader		header;
	private IncludeDeclaration	includeDeclaration;
	
	public IncludeParser(IDyvilHeader header)
	{
		this.header = header;
	}
	
	public IncludeParser(IDyvilHeader header, IncludeDeclaration includeDeclaration)
	{
		this.header = header;
		this.includeDeclaration = includeDeclaration;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case INCLUDE:
			if (type == DyvilKeywords.INCLUDE)
			{
				this.mode = NAME;
				this.includeDeclaration = new IncludeDeclaration(token.raw());
				return;
			}
			pm.report(token, "Invalid Include Declaration - 'include' expected");
			return;
		case NAME:
			this.mode = DOT;
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				this.includeDeclaration.addNamePart(name);
				return;
			}
			pm.report(token, "Invalid Include Declaration - Identifier expected");
			return;
		case DOT:
			if (type == BaseSymbols.SEMICOLON || type == Tokens.EOF)
			{
				this.header.addInclude(this.includeDeclaration);
				pm.popParser();
				return;
			}
			if (type == BaseSymbols.DOT)
			{
				this.mode = NAME;
				return;
			}
			pm.report(token, "Invalid Include Declaration - '.' expected");
			return;
		}
	}
}

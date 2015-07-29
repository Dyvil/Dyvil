package dyvil.tools.compiler.parser.imports;

import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

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
			if (type == Keywords.INCLUDE)
			{
				this.mode = NAME;
				this.includeDeclaration = new IncludeDeclaration(token.raw());
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Include Declaration - 'include' expected"));
			return;
		case NAME:
			this.mode = DOT;
			if (ParserUtil.isIdentifier(type))
			{
				Name name = token.nameValue();
				this.includeDeclaration.addNamePart(name);
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Include Declaration - Identifier expected"));
			return;
		case DOT:
			if (type == Symbols.SEMICOLON)
			{
				this.header.addInclude(this.includeDeclaration);
				pm.popParser();
				return;
			}
			if (type == Symbols.DOT)
			{
				this.mode = NAME;
				return;
			}
			pm.report(new SyntaxError(token, "Invalid Include Declaration - '.' expected"));
			return;
		}
	}
}

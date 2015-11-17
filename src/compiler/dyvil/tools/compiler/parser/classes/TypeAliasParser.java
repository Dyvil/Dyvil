package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class TypeAliasParser extends Parser
{
	private static final int	TYPE	= 1;
	private static final int	NAME	= 2;
	private static final int	EQUAL	= 4;
	
	protected ITypeAliasMap	map;
	protected ITypeAlias	typeAlias;
	
	public TypeAliasParser(ITypeAliasMap map)
	{
		this.map = map;
		this.mode = TYPE;
	}
	
	public TypeAliasParser(ITypeAliasMap map, ITypeAlias typeAlias)
	{
		this.map = map;
		this.typeAlias = typeAlias;
		this.mode = NAME;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		switch (this.mode)
		{
		case 0:
			this.map.addTypeAlias(this.typeAlias);
			pm.popParser(true);
			return;
		case TYPE:
			this.mode = NAME;
			this.typeAlias = new TypeAlias();
			if (token.type() == DyvilKeywords.TYPE)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Type Alias - 'type' expected");
			return;
		case NAME:
			if (ParserUtil.isIdentifier(token.type()))
			{
				Name name = token.nameValue();
				this.typeAlias.setName(name);
				this.mode = EQUAL;
				return;
			}
			pm.skip();
			pm.popParser();
			pm.report(token, "Invalid Type Alias - Identifier expected");
			return;
		case EQUAL:
			this.mode = 0;
			pm.pushParser(pm.newTypeParser(this.typeAlias));
			if (token.type() == BaseSymbols.EQUALS)
			{
				return;
			}
			pm.reparse();
			pm.report(token, "Invalid Type Alias - '=' expected");
			return;
		}
	}
}

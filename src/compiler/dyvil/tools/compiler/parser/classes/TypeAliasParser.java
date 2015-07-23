package dyvil.tools.compiler.parser.classes;

import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.type.alias.ITypeAlias;
import dyvil.tools.compiler.ast.type.alias.ITypeAliasMap;
import dyvil.tools.compiler.ast.type.alias.TypeAlias;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

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
	public void reset()
	{
		this.mode = TYPE;
		this.typeAlias = null;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
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
			if (token.type() == Keywords.TYPE)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Type Alias - 'type' expected", true);
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
			throw new SyntaxError(token, "Invalid Type Alias - Identifier expected");
		case EQUAL:
			this.mode = 0;
			pm.pushParser(pm.newTypeParser(this.typeAlias));
			if (token.type() == Symbols.EQUALS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Type Alias - '=' expected", true);
		}
	}
}

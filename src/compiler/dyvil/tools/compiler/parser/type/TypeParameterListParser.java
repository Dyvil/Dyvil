package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.generic.ITypeParametric;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class TypeParameterListParser extends Parser
{
	private static final int TYPE_VARIABLE = 0;
	private static final int COMMA         = 1;

	protected ITypeParametric typeParameterized;
	
	public TypeParameterListParser(ITypeParametric typeParameterized)
	{
		this.typeParameterized = typeParameterized;
		this.mode = TYPE_VARIABLE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case TYPE_VARIABLE:
			this.mode = COMMA;
			pm.pushParser(new TypeParameterParser(this.typeParameterized), true);
			return;
		case COMMA:
			if (ParserUtil.isCloseBracket(type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = TYPE_VARIABLE;
			if (type != BaseSymbols.COMMA)
			{
				pm.report(token, "typeparameter.list.comma");
				pm.reparse();
			}
			return;
		}
	}
}

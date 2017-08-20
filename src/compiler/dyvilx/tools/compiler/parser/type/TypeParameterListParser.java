package dyvilx.tools.compiler.parser.type;

import dyvilx.tools.compiler.ast.generic.ITypeParametric;
import dyvilx.tools.compiler.parser.ParserUtil;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.token.IToken;

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
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE_VARIABLE:
			this.mode = COMMA;
			pm.pushParser(new TypeParameterParser(this.typeParameterized), true);
			return;
		case COMMA:
			if (ParserUtil.isCloseBracket(type) || TypeParser.isGenericEnd(token, type))
			{
				pm.popParser(true);
				return;
			}
			this.mode = TYPE_VARIABLE;
			if (!ParserUtil.isTerminator(type))
			{
				pm.report(token, "type_parameter.list.comma");
				pm.reparse();
			}
		}
	}
}

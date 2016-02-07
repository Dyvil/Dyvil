package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class TypeListParser extends Parser
{
	private static final int TYPE = 0;
	private static final int COMMA = 1;

	protected ITypeConsumer consumer;
	
	public TypeListParser(ITypeConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = TYPE;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			this.mode = 1;
			pm.pushParser(pm.newTypeParser(this.consumer), true);
			return;
		case COMMA:
			if (ParserUtil.isCloseBracket(type) || type == BaseSymbols.OPEN_CURLY_BRACKET || type == BaseSymbols.SEMICOLON)
			{
				pm.popParser(true);
				return;
			}
			this.mode = TYPE;
			if (type == BaseSymbols.COMMA)
			{
				return;
			}
			pm.report(token, "type.list.comma");
			return;
		}
	}
}

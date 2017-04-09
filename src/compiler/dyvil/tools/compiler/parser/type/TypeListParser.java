package dyvil.tools.compiler.parser.type;

import dyvil.tools.compiler.ast.consumer.ITypeConsumer;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class TypeListParser extends Parser
{
	private static final int TYPE      = 0;
	private static final int SEPARATOR = 1;

	protected ITypeConsumer consumer;

	private boolean closeAngle;

	public TypeListParser(ITypeConsumer consumer)
	{
		this.consumer = consumer;
		// this.mode = TYPE;
	}

	public TypeListParser(ITypeConsumer consumer, boolean closeAngle)
	{
		this.consumer = consumer;
		this.closeAngle = closeAngle;
		// this.mode = TYPE;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case TYPE:
			this.mode = SEPARATOR;

			pm.pushParser(new TypeParser(this.consumer, this.closeAngle), true);
			return;
		case SEPARATOR:
			switch (type)
			{
			//noinspection DefaultNotLastCaseInSwitch
			default:
				if (!ParserUtil.isCloseBracket(type) && !TypeParser.isGenericEnd(token, type))
				{
					break;
				}
				// Fallthrough
			case BaseSymbols.OPEN_CURLY_BRACKET:
			case BaseSymbols.EQUALS:
			case BaseSymbols.SEMICOLON:
			case Tokens.EOF:
				pm.popParser(true);
				return;
			}

			this.mode = TYPE;
			if (type != BaseSymbols.COMMA)
			{
				pm.report(token, "type.list.comma");
			}
		}
	}
}

package dyvilx.tools.compiler.parser.type;

import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public final class TypeListParser extends Parser
{
	private static final int TYPE      = 0;
	private static final int SEPARATOR = 1;

	protected Consumer<IType> consumer;

	private boolean closeAngle;

	public TypeListParser(Consumer<IType> consumer)
	{
		this.consumer = consumer;
		// this.mode = TYPE;
	}

	public TypeListParser(Consumer<IType> consumer, boolean closeAngle)
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
				if (!BaseSymbols.isCloseBracket(type) && !TypeParser.isGenericEnd(token, type))
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

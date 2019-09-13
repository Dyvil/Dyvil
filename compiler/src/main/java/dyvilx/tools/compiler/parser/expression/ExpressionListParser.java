package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ExpressionListParser extends Parser
{
	// =============== Constants ===============

	private static final int EXPRESSION = 0;
	private static final int COMMA      = 1;

	// =============== Fields ===============

	protected Consumer<IValue> consumer;

	// =============== Constructors ===============

	public ExpressionListParser(Consumer<IValue> consumer)
	{
		this.consumer = consumer;
		this.mode = EXPRESSION;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		if (BaseSymbols.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}

		switch (this.mode)
		{
		case EXPRESSION:
			this.mode = COMMA;
			pm.pushParser(new ExpressionParser(this.consumer), true);
			return;
		case COMMA:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (!token.isInferred())
				{
					pm.report(token, "expression.list.comma");
				}
				// Fallthrough
			case BaseSymbols.COMMA:
				this.mode = EXPRESSION;
				return;
			default:
				pm.report(token, "expression.list.comma");
				return;
			}
		}
	}
}

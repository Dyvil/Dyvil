package dyvilx.tools.gensrc.parser.expression;

import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;
import dyvilx.tools.gensrc.ast.expression.*;

import java.util.function.Consumer;

public class ExpressionParser extends Parser
{
	public static final int VALUE    = 0;
	public static final int LIST_END = 1;

	private final Consumer<Expression> consumer;

	private Expression value;

	public ExpressionParser(Consumer<Expression> consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case VALUE:
			switch (type)
			{
			case Tokens.INT:
				this.consumer.accept(new LongValue(token.raw(), token.intValue()));
				pm.popParser();
				return;
			case Tokens.LONG:
				this.consumer.accept(new LongValue(token.raw(), token.longValue()));
				pm.popParser();
				return;
			case Tokens.FLOAT:
				this.consumer.accept(new DoubleValue(token.raw(), token.floatValue()));
				pm.popParser();
				return;
			case Tokens.DOUBLE:
				this.consumer.accept(new DoubleValue(token.raw(), token.doubleValue()));
				pm.popParser();
				return;
			case Tokens.VERBATIM_CHAR:
			case Tokens.VERBATIM_STRING:
			case Tokens.SINGLE_QUOTED_STRING:
			case Tokens.STRING:
				this.consumer.accept(new StringValue(token.raw(), token.stringValue()));
				pm.popParser();
				return;
			case Tokens.LETTER_IDENTIFIER:
				this.consumer.accept(new VarAccess(token.raw(), token.nameValue()));
				pm.popParser();
				return;
			case BaseSymbols.OPEN_SQUARE_BRACKET:
				final ListExpression listExpression = new ListExpression(token);
				pm.pushParser(new ExpressionListParser(listExpression.getValues()));
				this.value = listExpression;
				this.mode = LIST_END;
				return;
			}
			pm.popParser(true);
			return;
		case LIST_END:
			if (type != BaseSymbols.CLOSE_SQUARE_BRACKET)
			{
				pm.report(token, "expression.list.close_bracket");
				return;
			}
			this.value.setPosition(this.value.getPosition().to(token));
			this.consumer.accept(this.value);
			pm.popParser();
			return;
		case END:
		}
	}
}

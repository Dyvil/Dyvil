package dyvil.tools.gensrc.parser.expression;

import dyvil.tools.gensrc.ast.expression.*;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ExpressionParser extends Parser
{
	private final Consumer<Expression> consumer;

	public ExpressionParser(Consumer<Expression> consumer)
	{
		this.consumer = consumer;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		switch (token.type())
		{
		case Tokens.INT:
		case Tokens.LONG:
			this.consumer.accept(new LongValue(token.raw(), token.longValue()));
			pm.popParser();
			return;
		case Tokens.FLOAT:
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
		}

		pm.report(token, "expression.empty");
		pm.popParser(true);
	}
}

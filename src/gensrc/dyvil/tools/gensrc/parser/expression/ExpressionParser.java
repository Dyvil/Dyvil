package dyvil.tools.gensrc.parser.expression;

import dyvil.tools.gensrc.ast.expression.Expression;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
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
	public void parse(IParserManager iParserManager, IToken iToken)
	{

	}
}

package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.consumer.IArgumentsConsumer;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.ArgumentMap;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ArgumentListParser
{
	public static void parseArguments(IParserManager pm, IToken next, IArgumentsConsumer consumer)
	{
		final int nextType = next.type();

		if (ParserUtil.isCloseBracket(nextType))
		{
			consumer.setArguments(EmptyArguments.VISIBLE);
			return;
		}
		if (ParserUtil.isIdentifier(nextType) && next.next().type() == BaseSymbols.COLON)
		{
			final ArgumentMap map = new ArgumentMap();
			pm.pushParser(new ExpressionMapParser(map));
			consumer.setArguments(map);
			return;
		}

		final ArgumentList list = new ArgumentList();
		pm.pushParser(new ExpressionListParser(list));
		consumer.setArguments(list);
	}
}

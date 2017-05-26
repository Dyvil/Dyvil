package dyvil.tools.gensrc.parser.expression;

import dyvil.tools.gensrc.ast.expression.ExpressionList;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class ExpressionListParser extends Parser
{
	private final ExpressionList list;

	public ExpressionListParser(ExpressionList list)
	{
		this.list = list;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		if (this.mode == 0)
		{
			pm.pushParser(new ExpressionParser(this.list::add));
			this.mode = 1;
		}
		else if (token.type() == BaseSymbols.COMMA)
		{
			this.mode = 0;
		}
		else
		{
			pm.popParser(true);
		}
	}
}

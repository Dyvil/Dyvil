package dyvilx.tools.gensrc.parser.expression;

import dyvilx.tools.gensrc.ast.expression.ExpressionList;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

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
			pm.pushParser(new ExpressionParser(this.list::add), true);
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

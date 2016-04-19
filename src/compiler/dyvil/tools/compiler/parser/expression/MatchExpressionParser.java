package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.expression.MatchExpr;
import dyvil.tools.compiler.parser.pattern.CaseParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class MatchExpressionParser extends Parser
{
	private static final int OPEN_BRACKET = 0;
	private static final int CASE         = 1;
	private static final int SEPARATOR    = 2;

	protected MatchExpr matchExpression;

	public MatchExpressionParser(MatchExpr matchExpression)
	{
		this.matchExpression = matchExpression;
		// this.mode = OPEN_BRACKET;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case OPEN_BRACKET:
			if (type == DyvilKeywords.CASE)
			{
				pm.pushParser(new CaseParser(this.matchExpression), true);
				this.mode = END;
				return;
			}
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "match.invalid");
				return;
			}
			this.mode = SEPARATOR;
			pm.pushParser(new CaseParser(this.matchExpression));
			return;
		case CASE:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				return;
			}
			if (type == BaseSymbols.SEMICOLON)
			{
				return;
			}

			this.mode = SEPARATOR;
			pm.pushParser(new CaseParser(this.matchExpression), true);
			return;
		case SEPARATOR:
			if (type == BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.popParser();
				return;
			}
			this.mode = CASE;
			if (type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "match.case.end");
			}
			return;
		case END:
			pm.popParser(true);
		}
	}
}

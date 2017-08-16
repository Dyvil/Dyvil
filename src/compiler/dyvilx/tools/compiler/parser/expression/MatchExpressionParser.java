package dyvilx.tools.compiler.parser.expression;

import dyvilx.tools.compiler.ast.expression.MatchExpr;
import dyvilx.tools.compiler.parser.pattern.CaseParser;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class MatchExpressionParser extends Parser
{
	private static final int MATCH          = 0;
	private static final int EXPRESSION     = 1;
	private static final int SINGLE_CASE    = 2;
	private static final int EXPRESSION_END = 4;
	private static final int CASE           = 8;
	private static final int CASE_SEPARATOR = 16;

	protected MatchExpr matchExpression;

	public MatchExpressionParser(MatchExpr matchExpression)
	{
		this.matchExpression = matchExpression;

		if (matchExpression.getValue() == null)
		{
			// match ... { ... }
			this.mode = EXPRESSION;
		}
		else
		{
			// ... match { ... }
			this.mode = SINGLE_CASE;
		}
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case MATCH:
			this.mode = EXPRESSION;
			if (type == DyvilKeywords.MATCH)
			{
				return;
			}

			pm.report(token, "match.match_keyword");
			// Fallthrough
		case EXPRESSION:
			this.mode = EXPRESSION_END;
			pm.pushParser(new ExpressionParser(this.matchExpression).withFlags(IGNORE_STATEMENT), true);
			return;
		case EXPRESSION_END:
			if (type == BaseSymbols.COLON)
			{
				this.mode = SINGLE_CASE;
				return;
			}
			// Fallthrough
		case SINGLE_CASE:
			if (type == DyvilKeywords.CASE)
			{
				pm.pushParser(new CaseParser(this.matchExpression), true);
				this.mode = END;
				return;
			}
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "match.brace_case");
				return;
			}
			this.mode = CASE_SEPARATOR;
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

			this.mode = CASE_SEPARATOR;
			pm.pushParser(new CaseParser(this.matchExpression), true);
			return;
		case CASE_SEPARATOR:
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

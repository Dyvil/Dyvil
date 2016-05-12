package dyvil.tools.compiler.parser.expression;

import dyvil.tools.compiler.ast.expression.MatchExpr;
import dyvil.tools.compiler.parser.pattern.CaseParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_CLOSURE;
import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_COLON;

public class MatchExpressionParser extends Parser
{
	private static final int EXPRESSION     = 0;
	private static final int CLOSE_PAREN    = 1;
	private static final int SINGLE_CASE    = 2;
	private static final int VALUE_END      = 4;
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
		case EXPRESSION:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = CLOSE_PAREN;
				pm.pushParser(new ExpressionParser(this.matchExpression));
				return;
			}

			this.mode = VALUE_END;
			pm.pushParser(new ExpressionParser(this.matchExpression).withFlag(IGNORE_CLOSURE | IGNORE_COLON), true);
			return;
		case VALUE_END:
			if (type == BaseSymbols.COLON)
			{
				this.mode = SINGLE_CASE;
				return;
			}
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "match.brace");
				return;
			}
			this.mode = CASE_SEPARATOR;
			pm.pushParser(new CaseParser(this.matchExpression));
			return;
		case CLOSE_PAREN:
			this.mode = SINGLE_CASE;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "match.close_paren");
				pm.reparse();
			}
			return;
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

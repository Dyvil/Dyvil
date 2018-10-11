package dyvilx.tools.compiler.parser.pattern;

import dyvilx.tools.compiler.ast.expression.MatchCase;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.statement.StatementListParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.*;

public class CaseParser extends Parser
{
	// =============== Constants ===============

	private static final int CASE      = 0;
	private static final int CONDITION = 1;
	private static final int ACTION    = 2;

	// =============== Fields ===============

	protected final Consumer<MatchCase> consumer;

	private MatchCase matchCase;

	// =============== Constructors ===============

	public CaseParser(Consumer<MatchCase> consumer)
	{
		this.consumer = consumer;
		// this.mode = CASE
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case CASE:
			if (type == DyvilKeywords.CASE)
			{
				this.matchCase = new MatchCase();
				this.mode = CONDITION;
				pm.pushParser(new PatternParser(this.matchCase::setPattern));
				return;
			}
			if (BaseSymbols.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}
			pm.report(token, "match.case");
			return;
		case CONDITION:
			if (type == DyvilKeywords.IF)
			{
				this.mode = ACTION;
				pm.pushParser(new ExpressionParser(this.matchCase::setCondition)
					              .withFlags(IGNORE_COLON | IGNORE_CLOSURE | IGNORE_LAMBDA));
				return;
			}
			// Fallthrough
		case ACTION:
			this.mode = END;
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new StatementListParser(this.matchCase::setAction), true);
				return;
			}
			pm.pushParser(new ExpressionParser(this.matchCase::setAction));
			if (type != BaseSymbols.COLON && type != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.reparse();
				pm.report(token, "match.case.action");
			}
			return;
		case END:
			pm.popParser(true);
			this.consumer.accept(this.matchCase);
		}
	}
}

package dyvil.tools.compiler.parser.pattern;

import dyvil.tools.compiler.ast.consumer.ICaseConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.MatchCase;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.statement.StatementListParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.expression.ExpressionParser.*;

public class CaseParser extends Parser implements IValueConsumer
{
	private static final int CASE      = 0;
	private static final int CONDITION = 1;
	private static final int ACTION    = 2;

	protected final ICaseConsumer caseConsumer;

	private MatchCase matchCase;

	public CaseParser(ICaseConsumer caseConsumer)
	{
		this.caseConsumer = caseConsumer;
		// this.mode = CASE
	}

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
				pm.pushParser(new PatternParser(this.matchCase));
				return;
			}
			if (ParserUtil.isTerminator(type))
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
				pm.pushParser(new ExpressionParser(this).withFlag(IGNORE_COLON | IGNORE_CLOSURE | IGNORE_LAMBDA));
				return;
			}
			// Fallthrough
		case ACTION:
			this.mode = END;
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.pushParser(new StatementListParser(this), true);
				return;
			}
			pm.pushParser(new ExpressionParser(this));
			if (type != BaseSymbols.COLON && type != DyvilSymbols.DOUBLE_ARROW_RIGHT)
			{
				pm.reparse();
				pm.report(token, "match.case.action");
			}
			return;
		case END:
			pm.popParser(true);
			this.caseConsumer.addCase(this.matchCase);
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case ACTION:
			this.matchCase.setCondition(value);
			return;
		case END:
			this.matchCase.setAction(value);
		}
	}
}

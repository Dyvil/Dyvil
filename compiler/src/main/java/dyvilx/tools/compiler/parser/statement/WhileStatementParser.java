package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.statement.loop.WhileStatement;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class WhileStatementParser extends Parser
{
	// =============== Constants ===============

	protected static final int WHILE     = 0;
	protected static final int CONDITION = 1;
	protected static final int ACTION    = 2;

	// =============== Fields ===============

	protected final WhileStatement statement;

	// =============== Constructors ===============

	public WhileStatementParser(WhileStatement statement)
	{
		this.statement = statement;
		this.mode = CONDITION;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case WHILE:
			if (type != DyvilKeywords.WHILE)
			{
				pm.report(token, "while.keyword");
				return;
			}

			this.mode = CONDITION;
			return;
		case CONDITION:
			pm.pushParser(new ExpressionParser(this.statement::setCondition).withFlags(IGNORE_STATEMENT), true);
			this.mode = ACTION;
			return;
		case ACTION:
			if (BaseSymbols.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				ForStatementParser.reportSingleStatement(pm, token, "while.single.deprecated");
			}
			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
			return;
		}
	}
}

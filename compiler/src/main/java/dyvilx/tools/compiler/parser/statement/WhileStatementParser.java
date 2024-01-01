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

	private static final int WHILE = 0;
	private static final int CONDITION = 1;
	private static final int ACTION = 2;

	// =============== Fields ===============

	protected final WhileStatement statement;

	// =============== Constructors ===============

	public WhileStatementParser(WhileStatement statement)
	{
		this.statement = statement;
		this.mode = WHILE;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case WHILE:
			this.mode = CONDITION;
			if (type != DyvilKeywords.WHILE)
			{
				pm.reparse();
				pm.report(token, "while.keyword");
			}
			return;
		case CONDITION:
			pm.pushParser(new ExpressionParser(this.statement::setCondition).withFlags(IGNORE_STATEMENT), true);
			this.mode = ACTION;
			return;
		case ACTION:
			pm.pushParser(new StatementListParser(this.statement::setAction), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
			return;
		}
	}
}

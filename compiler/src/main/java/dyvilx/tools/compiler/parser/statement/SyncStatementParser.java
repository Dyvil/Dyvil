package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.statement.SyncStatement;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class SyncStatementParser extends Parser
{
	// =============== Constants ===============

	private static final int SYNCHRONIZED = 0;
	private static final int LOCK = 1;
	private static final int ACTION = 2;

	// =============== Fields ===============

	protected final SyncStatement statement;

	// =============== Constructors ===============

	public SyncStatementParser(SyncStatement statement)
	{
		this.statement = statement;
		this.mode = SYNCHRONIZED;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case SYNCHRONIZED:
			this.mode = LOCK;
			if (type != DyvilKeywords.SYNCHRONIZED)
			{
				pm.reparse();
				pm.report(token, "synchronized.keyword");
			}
			return;
		case LOCK:
			pm.pushParser(new ExpressionParser(this.statement::setLock).withFlags(IGNORE_STATEMENT), true);
			this.mode = ACTION;
			return;
		case ACTION:
			pm.pushParser(new StatementListParser(this.statement::setAction), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
		}
	}
}

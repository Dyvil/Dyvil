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
	private static final int LOCK         = 1;
	private static final int ACTION       = 2;

	// =============== Fields ===============

	protected final SyncStatement statement;

	// =============== Constructors ===============

	public SyncStatementParser(SyncStatement statement)
	{
		this.statement = statement;
		this.mode = LOCK;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case SYNCHRONIZED:
			if (type != DyvilKeywords.SYNCHRONIZED)
			{
				pm.report(token, "synchronized.keyword");
				return;
			}

			this.mode = LOCK;
			return;
		case LOCK:
			pm.pushParser(new ExpressionParser(this.statement::setLock).withFlags(IGNORE_STATEMENT));
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
				ForStatementParser.reportSingleStatement(pm, token, "synchronized.single.deprecated");
			}

			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
		}
	}
}

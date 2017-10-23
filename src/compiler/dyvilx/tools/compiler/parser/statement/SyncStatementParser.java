package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.statement.SyncStatement;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class SyncStatementParser extends Parser implements IValueConsumer
{
	private static final int SYNCHRONIZED = 0;
	private static final int LOCK         = 1;
	private static final int SEPARATOR    = 2;
	private static final int ACTION       = 3;

	protected SyncStatement statement;

	public SyncStatementParser(SyncStatement statement)
	{
		this.statement = statement;
		this.mode = LOCK;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case SYNCHRONIZED:
			this.mode = LOCK;
			if (type == DyvilKeywords.SYNCHRONIZED)
			{
				return;
			}

			pm.report(token, "sync.synchronized");
			// Fallthrough
		case LOCK:
			pm.pushParser(new ExpressionParser(this).withFlags(IGNORE_STATEMENT));
			this.mode = SEPARATOR;
			return;
		case SEPARATOR:
			switch (type)
			{
			case Tokens.EOF:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				pm.popParser(true);
				return;
			}

			this.mode = END;
			pm.pushParser(new ExpressionParser(this), true);
			// pm.report(token, "sync.separator");
			return;
		case ACTION:
			if (BaseSymbols.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}

			pm.pushParser(new ExpressionParser(this), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case SEPARATOR:
			this.statement.setLock(value);
			return;
		case END:
			this.statement.setAction(value);
		}
	}
}

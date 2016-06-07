package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.SyncStatement;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_CLOSURE;
import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_COLON;

public class SyncStatementParser extends Parser implements IValueConsumer
{
	private static final int SYNCHRONIZED = 0;
	private static final int LOCK         = 1;
	private static final int LOCK_END     = 2;
	private static final int SEPARATOR    = 4;
	private static final int ACTION       = 8;

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
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = LOCK_END;
				pm.pushParser(new ExpressionParser(this));
			}
			else
			{
				this.mode = SEPARATOR;
				pm.pushParser(new ExpressionParser(this).withFlag(IGNORE_CLOSURE | IGNORE_COLON));
			}
			return;
		case LOCK_END:
			this.mode = ACTION;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "sync.close_paren");
			}
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
			case BaseSymbols.COLON:
				this.mode = END;
				pm.pushParser(new ExpressionParser(this));
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.mode = END;
				pm.pushParser(new StatementListParser(this), true);
				return;
			}

			pm.report(token, "sync.separator");
			return;
		case ACTION:
			if (ParserUtil.isTerminator(type) && !token.isInferred())
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
		case LOCK_END:
			this.statement.setLock(value);
			return;
		case END:
			this.statement.setAction(value);
		}
	}
}

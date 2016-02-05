package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.SyncStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class SyncStatementParser extends Parser implements IValueConsumer
{
	private static final int END      = 0;
	private static final int LOCK     = 1;
	private static final int LOCK_END = 2;
	private static final int ACTION   = 4;
	
	protected SyncStatement statement;
	
	public SyncStatementParser(SyncStatement statement)
	{
		this.statement = statement;
		this.mode = LOCK;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		switch (this.mode)
		{
		case LOCK:
			this.mode = LOCK_END;
			if (token.type() == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			pm.reparse();
			pm.report(token, "sync.open_paren");
			return;
		case LOCK_END:
			this.mode = ACTION;
			if (token.type() != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "sync.close_paren");
			}
			return;
		case ACTION:
			if (ParserUtil.isTerminator(token.type()) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = -1;
			return;
		case END:
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case LOCK_END:
			this.statement.setLock(value);
			break;
		case -1:
			this.statement.setAction(value);
			break;
		}
	}
}

package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.SyncStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public class SyncStatementParser extends Parser implements IValueConsumer
{
	public static final int	LOCK		= 1;
	public static final int	LOCK_END	= 2;
	public static final int	THEN		= 4;
	
	protected SyncStatement statement;
	
	public SyncStatementParser(SyncStatement statement)
	{
		this.statement = statement;
		this.mode = LOCK;
	}
	
	@Override
	public void reset()
	{
		this.mode = LOCK;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == -1)
		{
			pm.popParser(true);
			return;
		}
		
		int type = token.type();
		if (this.mode == LOCK)
		{
			this.mode = LOCK_END;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			throw new SyntaxError(token, "Invalid Synchronized Block - '(' expected", true);
		}
		if (this.mode == LOCK_END)
		{
			this.mode = THEN;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid Synchronized Block - ')' expected", true);
		}
		if (this.mode == THEN)
		{
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = -1;
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == LOCK_END)
		{
			this.statement.lock = value;
		}
		else if (this.mode == -1)
		{
			this.statement.block = value;
		}
	}
}

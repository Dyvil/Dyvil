package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.WhileStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

public final class WhileStatementParser extends Parser implements IValueConsumer
{
	public static final int		CONDITION		= 1;
	public static final int		CONDITION_END	= 2;
	public static final int		BLOCK			= 4;
	
	protected WhileStatement	statement;
	
	public WhileStatementParser(WhileStatement statement)
	{
		this.statement = statement;
		this.mode = CONDITION;
	}
	
	@Override
	public void reset()
	{
		this.mode = CONDITION;
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
		if (this.mode == CONDITION)
		{
			this.mode = CONDITION_END;
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionParser(this));
				return;
			}
			throw new SyntaxError(token, "Invalid While Statement - '(' expected", true);
		}
		if (this.mode == CONDITION_END)
		{
			this.mode = BLOCK;
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid While Statement - ')' expected", true);
		}
		if (this.mode == BLOCK)
		{
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = -1;
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == CONDITION_END)
		{
			this.statement.condition = value;
		}
		else if (this.mode == -1)
		{
			this.statement.action = value;
		}
	}
}

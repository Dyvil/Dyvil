package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class IfStatementParser extends Parser implements IValued
{
	public static final int	IF				= 1;
	public static final int	CONDITION_END	= 2;
	public static final int	THEN			= 4;
	public static final int	ELSE			= 8;
	
	protected IfStatement	statement;
	
	public IfStatementParser(IfStatement statement)
	{
		this.mode = IF;
		this.statement = statement;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == -1)
		{
			pm.popParser(true);
			return true;
		}
		
		int type = token.type();
		if (this.mode == IF)
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return true;
			}
			return false;
		}
		if (this.mode == CONDITION_END)
		{
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				this.mode = THEN;
				return true;
			}
			return false;
		}
		if (this.mode == THEN)
		{
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return true;
			}
			
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = ELSE;
			return true;
		}
		if (this.mode == ELSE)
		{
			if (type == Tokens.ELSE)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = -1;
				return true;
			}
			
			pm.popParser(true);
			return true;
		}
		
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == CONDITION_END)
		{
			this.statement.setCondition(value);
		}
		else if (this.mode == ELSE)
		{
			this.statement.setThen(value);
		}
		else if (this.mode == -1)
		{
			this.statement.setElse(value);
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}

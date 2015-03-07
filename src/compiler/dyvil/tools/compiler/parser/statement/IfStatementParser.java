package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
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
		this.statement = statement;
		this.mode = IF;
	}
	
	@Override
	public void reset()
	{
		this.mode = IF;
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
		if (this.mode == IF)
		{
			this.mode = CONDITION_END;
			pm.pushParser(new ExpressionParser(this));
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid if statement - '(' expected", true);
		}
		if (this.mode == CONDITION_END)
		{
			this.mode = THEN;
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				return;
			}
			throw new SyntaxError(token, "Invalid if statement - ')' expected", true);
		}
		if (this.mode == THEN)
		{
			if (ParserUtil.isTerminator(type))
			{
				// 'else' on new line after inserted semicolon
				if (token.next().type() == Tokens.ELSE)
				{
					pm.skip(2);
					pm.pushParser(new ExpressionParser(this));
					this.mode = -1;
					return;
				}
				
				pm.popParser(true);
				return;
			}
			
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = ELSE;
			return;
		}
		if (this.mode == ELSE)
		{
			if (type == Tokens.ELSE)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = -1;
				return;
			}
			
			pm.popParser(true);
		}
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

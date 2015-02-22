package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.statement.DoStatement;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class DoStatementParser extends Parser implements IValued
{
	public static final int	DO				= 1;
	public static final int	WHILE			= 2;
	public static final int	CONDITION		= 4;
	public static final int	CONDITION_END	= 8;
	
	public DoStatement		statement;
	
	public DoStatementParser(DoStatement statement)
	{
		this.statement = statement;
		this.mode = DO;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == DO)
		{
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = WHILE;
			return true;
		}
		int type = token.type();
		if (this.mode == WHILE)
		{
			if (ParserUtil.isTerminator(type))
			{
				if (token.next().type() == Tokens.WHILE)
				{
					pm.skip(1);
					this.mode = CONDITION;
					return true;
				}
			}
			else if (type == Tokens.WHILE)
			{
				this.mode = CONDITION;
				return true;
			}
			
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid do-while statement - 'while' expected");
		}
		if (this.mode == CONDITION)
		{
			if (type == Tokens.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return true;
			}
			
			pm.pushParser(new ExpressionParser(this));
			this.mode = CONDITION_END;
			throw new SyntaxError(token, "Invalid do-while statement - '(' expected");
		}
		if (this.mode == CONDITION_END)
		{
			if (type == Tokens.CLOSE_PARENTHESIS)
			{
				pm.popParser();
				return true;
			}
			
			pm.popParser();
			throw new SyntaxError(token, "Invalid do-while statement - ')' expected");
		}
		return false;
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == WHILE)
		{
			this.statement.then = value;
		}
		else if (this.mode == CONDITION_END)
		{
			this.statement.condition = value;
		}
	}
}

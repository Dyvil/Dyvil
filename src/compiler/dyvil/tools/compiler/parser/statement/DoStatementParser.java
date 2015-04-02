package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.statement.DoStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.ParserUtil;

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
	public void reset()
	{
		this.mode = DO;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == DO)
		{
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = WHILE;
			return;
		}
		int type = token.type();
		if (this.mode == WHILE)
		{
			if (ParserUtil.isTerminator(type))
			{
				if (token.next().type() == Keywords.WHILE)
				{
					pm.skip(1);
					this.mode = CONDITION;
					return;
				}
			}
			else if (type == Keywords.WHILE)
			{
				this.mode = CONDITION;
				return;
			}
			
			pm.popParser(true);
			throw new SyntaxError(token, "Invalid Do-While Statement - 'while' expected");
		}
		if (this.mode == CONDITION)
		{
			if (type == Symbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = CONDITION_END;
				return;
			}
			
			pm.pushParser(new ExpressionParser(this));
			this.mode = CONDITION_END;
			throw new SyntaxError(token, "Invalid Do-While Statement - '(' expected");
		}
		if (this.mode == CONDITION_END)
		{
			if (type == Symbols.CLOSE_PARENTHESIS)
			{
				pm.popParser();
				return;
			}
			
			pm.popParser();
			throw new SyntaxError(token, "Invalid Do-While Statement - ')' expected");
		}
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
			this.statement.action = value;
		}
		else if (this.mode == CONDITION_END)
		{
			this.statement.condition = value;
		}
	}
}

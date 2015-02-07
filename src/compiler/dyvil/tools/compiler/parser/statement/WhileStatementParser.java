package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.statement.WhileStatement;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.compiler.util.Tokens;

public class WhileStatementParser extends Parser implements IValued
{
	public static final int		CONDITION		= 1;
	public static final int		CONDITION_END	= 2;
	public static final int		THEN			= 4;
	
	protected WhileStatement	statement;
	
	public WhileStatementParser(WhileStatement statement)
	{
		this.mode = CONDITION;
		this.statement = statement;
	}
	
	@Override
	public boolean parse(ParserManager pm, IToken token) throws SyntaxError
	{
		if (this.mode == -1)
		{
			pm.popParser(true);
			return true;
		}
		
		int type = token.type();
		if (this.mode == CONDITION)
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
			this.mode = -1;
			return true;
		}
		
		return false;
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
			this.statement.then = value;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}

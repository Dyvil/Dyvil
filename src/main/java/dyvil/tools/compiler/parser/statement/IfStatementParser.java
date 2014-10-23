package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

public class IfStatementParser extends Parser implements IValued
{
	public static final int	IF		= 1;
	public static final int	THEN	= 2;
	public static final int	ELSE	= 3;
	
	protected IContext	context;
	protected IfStatement	statement;
	
	public IfStatementParser(IContext context, IfStatement statement)
	{
		this.context = context;
		this.statement = statement;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == 0)
		{
			if ("(".equals(value))
			{
				this.mode = IF;
				return true;
			}
		}
		if (this.mode == IF)
		{
			if (")".equals(value))
			{
				this.mode = THEN;
				return true;
			}
			else
			{
				pm.pushParser(new ExpressionParser(this.context, this), token);
				return true;
			}
		}
		if (this.mode == THEN)
		{
			pm.pushParser(new ExpressionParser(this.context, this, true), token);
			return true;
		}
		if (this.mode == ELSE)
		{
			if ("else".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.context, this, true));
				return true;
			}
		}
		
		if (this.statement.getThen() != null)
		{
			pm.popParser(token);
			return true;
		}
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == IF)
		{
			this.statement.setCondition(value);
		}
		else if (this.mode == THEN)
		{
			this.statement.setThen(value);
			this.mode = ELSE;
		}
		else if (this.mode == ELSE)
		{
			this.statement.setElse(value);
			this.mode = -1;
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}

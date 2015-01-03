package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.api.IContext;
import dyvil.tools.compiler.ast.api.IValue;
import dyvil.tools.compiler.ast.api.IValued;
import dyvil.tools.compiler.ast.statement.WhileStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserManager;
import dyvil.tools.compiler.parser.expression.ExpressionParser;

public class WhileStatementParser extends Parser implements IValued
{
	public static final int		CONDITION		= 1;
	public static final int		CONDITION_END	= 2;
	public static final int		THEN			= 4;
	
	protected IContext			context;
	protected WhileStatement	statement;
	
	public WhileStatementParser(IContext context, WhileStatement statement)
	{
		this.mode = CONDITION;
		this.context = context;
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
		if (this.mode == CONDITION)
		{
			if ("(".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.context, this));
				this.mode = CONDITION_END;
				return true;
			}
			return false;
		}
		if (this.mode == CONDITION_END)
		{
			if (")".equals(value))
			{
				this.mode = THEN;
				return true;
			}
			return false;
		}
		if (this.mode == THEN)
		{
			if (!";".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.context, this), true);
			}
			this.mode = -1;
			return true;
		}
		
		if ("}".equals(value))
		{
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
		else if (this.mode == -1)
		{
			this.statement.setThen(value);
		}
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
}

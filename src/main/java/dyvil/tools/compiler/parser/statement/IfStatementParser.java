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
	public static final int	ELSE	= 4;
	public static final int END = 8;
	
	protected IContext		context;
	protected IfStatement	statement;
	
	public IfStatementParser(IContext context, IfStatement statement)
	{
		this.mode = IF;
		this.context = context;
		this.statement = statement;
	}
	
	@Override
	public boolean parse(ParserManager pm, String value, IToken token) throws SyntaxError
	{
		if (this.mode == END)
		{
			pm.popParser(true);
			return true;
		}
		if (this.mode == IF)
		{
			pm.pushParser(new ExpressionParser(this.context, this), true);
			this.mode = THEN;
			return true;
		}
		if (this.mode == THEN)
		{
			if (!";".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.context, this), true);
			}
			this.mode = ELSE;
			return true;
		}
		if (this.mode == ELSE)
		{
			if ("else".equals(value))
			{
				pm.pushParser(new ExpressionParser(this.context, this));
				this.mode = END;
				return true;
			}
			else if (";".equals(value))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == THEN)
		{
			this.statement.setCondition(value);
		}
		else if (this.mode == ELSE)
		{
			this.statement.setThen(value);
		}
		else if (this.mode == END)
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

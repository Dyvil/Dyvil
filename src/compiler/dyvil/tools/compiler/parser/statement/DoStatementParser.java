package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.DoStatement;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.token.IToken;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.Keywords;
import dyvil.tools.compiler.transform.Symbols;

public class DoStatementParser extends Parser implements IValueConsumer
{
	public static final int	DO		= 1;
	public static final int	WHILE	= 2;
	public static final int	END		= 4;
	
	public DoStatement statement;
	
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
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = WHILE;
			return;
		}
		int type = token.type();
		if (this.mode == WHILE)
		{
			if (type == Keywords.WHILE)
			{
				this.mode = END;
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			
			if (type == Symbols.SEMICOLON)
			{
				if (token.next().type() == Keywords.WHILE)
				{
					this.mode = END;
					pm.skip(1);
					pm.pushParser(pm.newExpressionParser(this));
					return;
				}
			}
			
			pm.popParser(true);
			return;
		}
		if (this.mode == END)
		{
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		if (this.mode == WHILE)
		{
			this.statement.action = value;
		}
		else if (this.mode == END)
		{
			this.statement.condition = value;
		}
	}
}

package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.loop.DoStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class DoStatementParser extends Parser implements IValueConsumer
{
	public static final int DO    = 1;
	public static final int WHILE = 2;
	
	public DoStatement statement;
	
	public DoStatementParser(DoStatement statement)
	{
		this.statement = statement;
		this.mode = DO;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		switch (this.mode)
		{
		case DO:
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = WHILE;
			return;
		case WHILE:
			int type = token.type();
			if (type == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			if (type == BaseSymbols.SEMICOLON)
			{
				if (token.next().type() == DyvilKeywords.WHILE)
				{
					this.mode = END;
					pm.skip(1);
					pm.pushParser(pm.newExpressionParser(this));
					return;
				}
			}
			// fallthrough
		case END:
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case WHILE:
			this.statement.setAction(value);
			break;
		case END:
			this.statement.setCondition(value);
			break;
		}
	}
}

package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.compiler.util.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class IfStatementParser extends Parser implements IValueConsumer
{
	public static final int	IF				= 1;
	public static final int	CONDITION_END	= 2;
	public static final int	THEN			= 4;
	public static final int	ELSE			= 8;
	
	protected IfStatement statement;
	
	public IfStatementParser(IfStatement statement)
	{
		this.statement = statement;
		this.mode = IF;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
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
			pm.pushParser(pm.newExpressionParser(this));
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "Invalid if statement - '(' expected");
			}
			return;
		}
		if (this.mode == CONDITION_END)
		{
			this.mode = THEN;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "Invalid if statement - ')' expected");
			}
			return;
		}
		if (this.mode == THEN)
		{
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}
			
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = ELSE;
			return;
		}
		if (this.mode == ELSE)
		{
			if (ParserUtil.isTerminator(type))
			{
				IToken next = token.next();
				if (next != null && next.type() == DyvilKeywords.ELSE)
				{
					return;
				}
				pm.popParser(true);
				return;
			}
			
			if (type == DyvilKeywords.ELSE)
			{
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = -1;
				return;
			}
			
			pm.popParser(true);
			return;
		}
	}
	
	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case CONDITION_END:
			this.statement.setCondition(value);
			return;
		case ELSE:
			this.statement.setThen(value);
			return;
		case -1:
			this.statement.setElse(value);
			return;
		}
	}
}

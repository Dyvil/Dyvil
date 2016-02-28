package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.IfStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class IfStatementParser extends Parser implements IValueConsumer
{
	private static final   int END           = -1;
	protected static final int IF            = 0;
	protected static final int CONDITION_END = 1;
	protected static final int THEN          = 2;
	protected static final int ELSE          = 4;
	
	protected IfStatement statement;
	
	public IfStatementParser(IfStatement statement)
	{
		this.statement = statement;
		this.mode = IF;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case END:
			pm.popParser(true);
			return;
		case IF:
			this.mode = CONDITION_END;
			pm.pushParser(pm.newExpressionParser(this));
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "if.open_paren");
			}
			return;
		case CONDITION_END:
			this.mode = THEN;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "if.close_paren");
			}
			return;
		case THEN:
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}

			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = ELSE;
			return;
		case ELSE:
			if (ParserUtil.isTerminator(type))
			{
				if (token.next().type() == DyvilKeywords.ELSE)
				{
					return;
				}
				pm.popParser(true);
				return;
			}
			
			if (type == DyvilKeywords.ELSE)
			{
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = END;
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

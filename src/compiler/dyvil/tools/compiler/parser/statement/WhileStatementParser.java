package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.loop.WhileStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public final class WhileStatementParser extends Parser implements IValueConsumer
{
	protected static final int CONDITION     = 1;
	protected static final int CONDITION_END = 2;
	protected static final int BLOCK         = 4;
	
	protected WhileStatement statement;
	
	public WhileStatementParser(WhileStatement statement)
	{
		this.statement = statement;
		this.mode = CONDITION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case CONDITION:
			this.mode = CONDITION_END;
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			pm.reparse();
			pm.report(token, "while.open_paren");
			return;
		case CONDITION_END:
			this.mode = BLOCK;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "while.close_paren");
			}
			return;
		case BLOCK:
			if (ParserUtil.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = END;
			return;
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
		case CONDITION_END:
			this.statement.setCondition(value);
			break;
		case END:
			this.statement.setAction(value);
			break;
		}
	}
}

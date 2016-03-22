package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.loop.RepeatStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class RepeatStatementParser extends Parser implements IValueConsumer
{
	protected static final int REPEAT = 1;
	protected static final int ACTION = 2;
	protected static final int WHILE  = 4;
	
	protected final RepeatStatement statement;
	
	public RepeatStatementParser(RepeatStatement statement)
	{
		this.statement = statement;
		this.mode = ACTION;
	}
	
	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case REPEAT:
			this.mode = ACTION;
			if (type != DyvilKeywords.DO)
			{
				pm.reparse();
				pm.report(token, "repeat.repeat");
			}
			return;
		case ACTION:
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = WHILE;
			return;
		case WHILE:
			if (type == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.pushParser(pm.newExpressionParser(this));
				return;
			}
			if (type == BaseSymbols.SEMICOLON && token.isInferred() && token.next().type() == DyvilKeywords.WHILE)
			{
				this.mode = END;
				pm.skip(1);
				pm.pushParser(pm.newExpressionParser(this));
				return;
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

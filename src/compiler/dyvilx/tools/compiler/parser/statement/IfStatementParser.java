package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class IfStatementParser extends Parser implements IValueConsumer
{
	protected static final int IF        = 0;
	protected static final int CONDITION = 1;
	protected static final int THEN      = 2;
	protected static final int ELSE      = 3;

	protected final IValueConsumer consumer;
	protected IfStatement statement;

	public IfStatementParser(IValueConsumer consumer)
	{
		this.consumer = consumer;
		this.mode = IF;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case IF:
			if (type != DyvilKeywords.IF)
			{
				pm.report(token, "if.if_keyword");
				return;
			}
			this.mode = CONDITION;
			this.statement = new IfStatement(token.raw());
			return;
		case CONDITION:
			pm.pushParser(new ExpressionParser(this).withFlags(IGNORE_STATEMENT), true);
			this.mode = THEN;
			return;
		case THEN:
			switch (type)
			{
			case Tokens.EOF:
			case BaseSymbols.SEMICOLON:
				this.end(pm);
				return;
			}

			this.mode = ELSE;
			pm.pushParser(new ExpressionParser(this), true);
			return;
		case ELSE:
			if (type == DyvilKeywords.ELSE || token.isInferred() && token.next().type() == DyvilKeywords.ELSE)
			{
				pm.pushParser(new ExpressionParser(this));
				this.mode = END;
				return;
			}

			// Fallthrough
		case END:
			this.end(pm);
			return;
		}
	}

	private void end(IParserManager pm)
	{
		this.consumer.setValue(this.statement);
		pm.popParser(true);
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case THEN:
			this.statement.setCondition(value);
			return;
		case ELSE:
			this.statement.setThen(value);
			return;
		case END:
			this.statement.setElse(value);
		}
	}
}

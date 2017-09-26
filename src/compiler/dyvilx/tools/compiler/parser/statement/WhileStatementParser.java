package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.statement.loop.WhileStatement;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public final class WhileStatementParser extends Parser implements IValueConsumer
{
	protected static final int WHILE         = 0;
	protected static final int CONDITION     = 1;
	protected static final int SEPARATOR     = 2;
	protected static final int BLOCK         = 3;

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
		case WHILE:
			this.mode = CONDITION;
			if (type == DyvilKeywords.SYNCHRONIZED)
			{
				return;
			}

			pm.report(token, "while.while_keyword");
			// Fallthrough
		case CONDITION:
			pm.pushParser(new ExpressionParser(this).withFlags(IGNORE_STATEMENT), true);
			this.mode = SEPARATOR;
			return;
		case SEPARATOR:
			switch (type)
			{
			case Tokens.EOF:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				pm.popParser(true);
				return;
			}

			this.mode = END;
			pm.pushParser(new ExpressionParser(this), true);
			// pm.report(token, "while.separator");
			return;
		case BLOCK:
			if (BaseSymbols.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			pm.pushParser(new ExpressionParser(this), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case SEPARATOR:
			this.statement.setCondition(value);
			return;
		case END:
			this.statement.setAction(value);
		}
	}
}

package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.consumer.IValueConsumer;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.statement.IfStatement;
import dyvilx.tools.compiler.parser.ParserUtil;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.transform.DyvilKeywords;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.*;

public class IfStatementParser extends Parser implements IValueConsumer
{
	protected static final int IF        = 0;
	protected static final int CONDITION = 1;
	protected static final int SEPARATOR = 2;
	protected static final int THEN      = 3;
	protected static final int ELSE      = 4;

	protected IfStatement statement;

	public IfStatementParser(IfStatement statement)
	{
		this.statement = statement;
		this.mode = CONDITION;
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
			this.mode = CONDITION;
			if (type == DyvilKeywords.IF)
			{
				return;
			}

			pm.report(token, "if.if");
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
			case BaseSymbols.COLON:
				this.mode = ELSE;
				pm.pushParser(new ExpressionParser(this));
				return;
			}

			this.mode = ELSE;
			pm.pushParser(new ExpressionParser(this), true);
			// pm.report(token, "if.separator");
			return;
		case THEN:
			if (ParserUtil.isTerminator(type))
			{
				pm.popParser(true);
				return;
			}

			pm.pushParser(new ExpressionParser(this), true);
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
				pm.pushParser(new ExpressionParser(this));
				this.mode = END;
				return;
			}

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
		case ELSE:
			this.statement.setThen(value);
			return;
		case END:
			this.statement.setElse(value);
		}
	}
}

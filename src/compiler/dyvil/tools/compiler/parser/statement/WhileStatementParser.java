package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.loop.WhileStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_CLOSURE;
import static dyvil.tools.compiler.parser.expression.ExpressionParser.IGNORE_COLON;

public final class WhileStatementParser extends Parser implements IValueConsumer
{
	protected static final int WHILE         = 0;
	protected static final int CONDITION     = 1;
	protected static final int CONDITION_END = 2;
	protected static final int SEPARATOR     = 4;
	protected static final int BLOCK         = 8;

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

			pm.report(token, "while.while");
			// Fallthrough
		case CONDITION:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = CONDITION_END;
				pm.pushParser(pm.newExpressionParser(this));
			}
			else
			{
				this.mode = SEPARATOR;
				pm.pushParser(pm.newExpressionParser(this).withFlag(IGNORE_CLOSURE | IGNORE_COLON), true);
			}
			return;
		case CONDITION_END:
			this.mode = BLOCK;
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "while.close_paren");
			}
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
				this.mode = END;
				pm.pushParser(new ExpressionParser(this));
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.mode = END;
				pm.pushParser(new StatementListParser(this), true);
				return;
			}

			pm.report(token, "while.separator");
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
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case SEPARATOR:
		case CONDITION_END:
			this.statement.setCondition(value);
			return;
		case END:
			this.statement.setAction(value);
		}
	}
}

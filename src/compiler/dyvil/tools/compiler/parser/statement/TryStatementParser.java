package dyvil.tools.compiler.parser.statement;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.statement.exception.CatchBlock;
import dyvil.tools.compiler.ast.statement.exception.TryStatement;
import dyvil.tools.compiler.parser.IParserManager;
import dyvil.tools.compiler.parser.Parser;
import dyvil.tools.compiler.parser.ParserUtil;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.transform.DyvilKeywords;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public final class TryStatementParser extends Parser implements IValueConsumer
{
	private static final int ACTION          = 1;
	private static final int CATCH           = 2;
	private static final int CATCH_OPEN      = 4;
	private static final int CATCH_CLOSE     = 8;
	private static final int CATCH_SEPARATOR = 16;

	protected TryStatement statement;
	private   CatchBlock   catchBlock;

	public TryStatementParser(TryStatement statement)
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
		case END:
			pm.popParser(true);
			return;
		case ACTION:
			pm.pushParser(pm.newExpressionParser(this), true);
			this.mode = CATCH;
			return;
		case CATCH:
			if (type == DyvilKeywords.CATCH)
			{
				this.statement.addCatchBlock(this.catchBlock = new CatchBlock());
				this.mode = CATCH_OPEN;
				return;
			}
			if (type == DyvilKeywords.FINALLY)
			{
				pm.pushParser(pm.newExpressionParser(this));
				this.mode = END;
				return;
			}
			if (ParserUtil.isTerminator(type))
			{
				int nextType = token.next().type();
				if (nextType == Tokens.EOF)
				{
					pm.popParser(true);
					return;
				}
				if (nextType == DyvilKeywords.CATCH || nextType == DyvilKeywords.FINALLY)
				{
					return;
				}
			}
			pm.popParser(true);
			return;
		case CATCH_OPEN:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.mode = CATCH_CLOSE;
				pm.pushParser(new VariableParser(this.catchBlock));
			}
			else
			{
				this.mode = CATCH_SEPARATOR;
				pm.pushParser(new VariableParser(this.catchBlock), true);
			}
			return;
		case CATCH_CLOSE:
			this.mode = CATCH;
			pm.pushParser(pm.newExpressionParser(this.catchBlock));
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.reparse();
				pm.report(token, "try.catch.close_paren");
			}
			return;
		case CATCH_SEPARATOR:
			switch (type)
			{
			case Tokens.EOF:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				this.mode = CATCH;
				return;
			case BaseSymbols.COLON:
				this.mode = CATCH;
				pm.pushParser(new ExpressionParser(this.catchBlock));
				return;
			case BaseSymbols.OPEN_CURLY_BRACKET:
				this.mode = CATCH;
				pm.pushParser(new StatementListParser(this.catchBlock), true);
				return;
			}

			pm.report(token, "try.catch.separator");
		}
	}

	@Override
	public void setValue(IValue value)
	{
		switch (this.mode)
		{
		case CATCH:
			this.statement.setAction(value);
			return;
		case END:
			this.statement.setFinallyBlock(value);
		}
	}
}

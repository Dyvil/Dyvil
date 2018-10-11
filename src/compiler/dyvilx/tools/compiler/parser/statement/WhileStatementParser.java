package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.statement.loop.WhileStatement;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import static dyvilx.tools.compiler.parser.expression.ExpressionParser.IGNORE_STATEMENT;

public class WhileStatementParser extends Parser
{
	// =============== Constants ===============

	protected static final int WHILE     = 0;
	protected static final int CONDITION = 1;
	protected static final int SEPARATOR = 2;
	protected static final int ACTION    = 3;

	// =============== Fields ===============

	protected final WhileStatement statement;

	// =============== Constructors ===============

	public WhileStatementParser(WhileStatement statement)
	{
		this.statement = statement;
		this.mode = CONDITION;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		int type = token.type();
		switch (this.mode)
		{
		case WHILE:
			if (type != DyvilKeywords.WHILE)
			{
				pm.report(token, "while.while_keyword");
				return;
			}

			this.mode = CONDITION;
			return;
		case CONDITION:
			pm.pushParser(new ExpressionParser(this.statement::setCondition).withFlags(IGNORE_STATEMENT), true);
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
			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			// pm.report(token, "while.separator");
			return;
		case ACTION:
			if (BaseSymbols.isTerminator(type) && !token.isInferred())
			{
				pm.popParser(true);
				return;
			}
			pm.pushParser(new ExpressionParser(this.statement::setAction), true);
			this.mode = END;
			return;
		case END:
			pm.popParser(true);
		}
	}
}

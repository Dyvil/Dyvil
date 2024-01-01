package dyvilx.tools.compiler.parser.statement;

import dyvilx.tools.compiler.ast.statement.loop.RepeatStatement;
import dyvilx.tools.compiler.parser.DyvilKeywords;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class RepeatStatementParser extends Parser
{
	// =============== Constants ===============

	private static final int REPEAT = 1;
	private static final int ACTION = 2;
	private static final int WHILE = 3;

	// =============== Fields ===============

	protected final RepeatStatement statement;

	// =============== Constructors ===============

	public RepeatStatementParser(RepeatStatement statement)
	{
		this.statement = statement;
		this.mode = REPEAT;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case REPEAT:
			this.mode = ACTION;
			if (type != DyvilKeywords.REPEAT)
			{
				pm.reparse();
				pm.report(token, "repeat.keyword");
			}
			return;
		case ACTION:
			pm.pushParser(new StatementListParser(this.statement::setAction), true);
			this.mode = WHILE;
			return;
		case WHILE:
			switch (type)
			{
				case BaseSymbols.SEMICOLON:
					if (token.isInferred() && token.next().type() == DyvilKeywords.WHILE)
					{
						return;
					}
					break; // end
				case DyvilKeywords.WHILE:
					this.mode = END;
					pm.pushParser(new ExpressionParser(this.statement::setCondition));
					return;
			}
			// fallthrough
		case END:
			pm.popParser(true);
		}
	}
}

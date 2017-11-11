package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.gensrc.ast.directive.ScopeDirective;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

public class ScopeDirectiveParser extends Parser
{
	protected static final int OPEN_PAREN  = 0;
	protected static final int CLOSE_PAREN = 1;
	protected static final int BODY        = 2;
	protected static final int BODY_END    = 3;

	private final StatementList list;

	private ScopeDirective directive = new ScopeDirective();

	public ScopeDirectiveParser(StatementList list)
	{
		this.list = list;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case OPEN_PAREN:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				this.directive = new ScopeDirective();
				pm.pushParser(new ExpressionParser(this.directive::setExpression));
				this.mode = CLOSE_PAREN;
				return;
			}
			// Fallthrough
		case BODY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				final StatementList body = new StatementList();
				pm.pushParser(new BlockParser(body));
				this.directive.setBlock(body);
				this.mode = BODY_END;
				return;
			}

			this.list.add(this.directive);
			pm.popParser(true);
			return;
		case CLOSE_PAREN:
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				this.mode = BODY;
				return;
			}

			pm.report(token, "directive.close_paren");
			return;
		case BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;
			this.list.add(this.directive);
			pm.popParser();
		}
	}
}

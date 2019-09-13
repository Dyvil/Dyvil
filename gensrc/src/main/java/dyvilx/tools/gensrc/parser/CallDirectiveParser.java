package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.parser.expression.ArgumentListParser;
import dyvilx.tools.gensrc.ast.directive.CallDirective;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class CallDirectiveParser extends Parser
{
	protected static final int NAME = 0;
	protected static final int OPEN_PAREN  = 1;
	protected static final int CLOSE_PAREN = 2;
	protected static final int BODY        = 3;
	protected static final int BODY_END    = 4;

	private final StatementList list;

	private CallDirective directive;

	public CallDirectiveParser(StatementList list)
	{
		this.list = list;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case NAME:
			if (Tokens.isIdentifier(type))
			{
				this.mode = OPEN_PAREN;
				this.directive = new CallDirective(token.raw(), token.nameValue());
				return;
			}

			pm.report(token, "directive.identifier");
			return;
		case OPEN_PAREN:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ArgumentListParser(this.directive::setArguments));
				this.mode = CLOSE_PAREN;
				return;
			}
			// Fallthrough
		case BODY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				final StatementList closure = new StatementList();
				pm.pushParser(new BlockParser(closure));
				this.directive.setBlock(closure);
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

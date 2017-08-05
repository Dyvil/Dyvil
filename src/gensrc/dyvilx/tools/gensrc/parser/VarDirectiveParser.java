package dyvilx.tools.gensrc.parser;

import dyvilx.tools.gensrc.ast.directive.DirectiveList;
import dyvilx.tools.gensrc.ast.var.DefineDirective;
import dyvilx.tools.gensrc.ast.var.NameDirective;
import dyvilx.tools.gensrc.ast.var.UndefineDirective;
import dyvilx.tools.gensrc.ast.var.VarDirective;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class VarDirectiveParser extends Parser
{
	private static final int KEYWORD     = 0;
	private static final int OPEN_PAREN  = 1;
	private static final int IDENTIFIER  = 2;
	private static final int CLOSE_PAREN = 3;
	private static final int BODY        = 4;
	private static final int BODY_END    = 5;

	private final DirectiveList directives;

	private VarDirective directive;

	public VarDirectiveParser(DirectiveList directives)
	{
		this.directives = directives;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case KEYWORD:
			boolean local = false;
			switch (type)
			{
			case GenSrcSymbols.LOCAL:
				local = true;
				// Fallthrough
			case GenSrcSymbols.DEFINE:
				this.directive = new DefineDirective(local, token.raw());
				break;
			case GenSrcSymbols.DELETE:
				local = true;
				// Fallthrough
			case GenSrcSymbols.UNDEFINE:
				this.directive = new UndefineDirective(local, token.raw());
				break;
			case GenSrcSymbols.NAME:
				this.directive = new NameDirective(token.raw());
				break;
			default:
				assert false;
			}

			this.mode = OPEN_PAREN;
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				this.directives.add(this.directive);
				pm.popParser(true);
				return;
			}

			this.mode = IDENTIFIER;
			return;
		case IDENTIFIER:
			if (type != Tokens.LETTER_IDENTIFIER)
			{
				pm.report(token, "var.identifier");
				if (type == BaseSymbols.CLOSE_PARENTHESIS)
				{
					this.mode = BODY;
				}
				return;
			}

			this.directive.setName(token.nameValue());
			this.mode = CLOSE_PAREN;
			return;
		case CLOSE_PAREN:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "var.close_paren");
				return;
			}

			this.mode = BODY;
			return;
		case BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				this.directives.add(this.directive);
				pm.popParser(true);
				return;
			}

			final DirectiveList body = new DirectiveList();
			pm.pushParser(new BlockParser(body));
			this.directive.setBody(body);
			this.mode = BODY_END;
			return;
		case BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;
			this.directives.add(this.directive);
			pm.popParser();
		}
	}
}

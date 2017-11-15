package dyvilx.tools.gensrc.parser;

import dyvilx.tools.gensrc.ast.Template;
import dyvilx.tools.gensrc.ast.header.TemplateDirective;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class TemplateDirectiveParser extends Parser
{
	protected static final int KEYWORD    = 0;
	protected static final int OPEN_PAREN = 1;
	protected static final int IDENTIFIER = 2;
	protected static final int SEPARATOR  = 3;

	protected final Template template;

	private TemplateDirective directive;

	public TemplateDirectiveParser(Template template)
	{
		this.template = template;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		switch (this.mode)
		{
		case KEYWORD:
			assert type == GenSrcSymbols.TEMPLATE;

			this.directive = new TemplateDirective(token.raw());
			if (this.template == null)
			{
				pm.report(token, "template.context");
			}
			this.mode = OPEN_PAREN;
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.report(token, "template.open_paren");
				pm.popParser(true);
				return;
			}
			if (token.next().type() == BaseSymbols.CLOSE_PARENTHESIS)
			{
				// common case: #template()
				pm.skip();
				if (this.template != null)
				{
					this.template.addTemplateDirective(this.directive);
				}
				pm.popParser();
				return;
			}

			this.mode = IDENTIFIER;
			return;
		case IDENTIFIER:
			if (Tokens.isIdentifier(type))
			{
				this.directive.addIdentifier(token.nameValue());
				this.mode = SEPARATOR;
				return;
			}
			pm.report(token, "template.identifier");
			// Fallthrough
		case SEPARATOR:
			switch (type)
			{
			case BaseSymbols.CLOSE_PARENTHESIS:
				if (this.template != null)
				{
					this.template.addTemplateDirective(this.directive);
				}
				pm.popParser();
				return;
			case BaseSymbols.COMMA:
			case BaseSymbols.SEMICOLON:
				this.mode = IDENTIFIER;
				return;
			case Tokens.EOF:
				pm.report(token, "template.separator");
				pm.popParser();
				return;
			}

			pm.reparse();
			this.mode = IDENTIFIER;
			pm.report(token, "template.separator");
			return;
		}
	}
}

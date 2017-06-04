package dyvil.tools.gensrc.parser;

import dyvil.tools.gensrc.ast.directive.*;
import dyvil.tools.gensrc.lexer.GenSrcSymbols;
import dyvil.tools.gensrc.parser.expression.ExpressionListParser;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class DirectiveParser extends Parser
{
	public static final int NAME        = 0;
	public static final int OPEN_PAREN  = 1;
	public static final int CLOSE_PAREN = 2;
	public static final int BODY        = 3;
	public static final int BODY_END    = 4;

	private final DirectiveList list;

	private BasicDirective directive;

	public DirectiveParser(DirectiveList list)
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
			switch (type)
			{
			case BaseSymbols.HASH:
				// ## -> do nothing
				this.list.add(Directive.LITERAL_HASH);
				pm.popParser();
				return;
			case GenSrcSymbols.IMPORT:
				this.directive = new ImportDirective(token.raw());
				break;
			case GenSrcSymbols.INCLUDE:
				this.directive = new IncludeDirective(token.raw());
				break;
			case BaseSymbols.OPEN_PARENTHESIS:
			case BaseSymbols.OPEN_CURLY_BRACKET:
				pm.reparse();
				this.directive = new ScopeDirective(token.raw());
				break;
			default:
				final Name name = token.nameValue();
				if (name == null)
				{
					pm.report(token, "directive.identifier");
					this.directive = new ScopeDirective(token.raw());
				}
				else
				{
					this.directive = new NamedDirective(token.raw(), name);
				}

				break;
			}

			this.mode = OPEN_PAREN;
			return;
		case OPEN_PAREN:
			if (type == BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.pushParser(new ExpressionListParser(this.directive.getArguments()));
				this.mode = CLOSE_PAREN;
				return;
			}
			// Fallthrough
		case BODY:
			if (type == BaseSymbols.OPEN_CURLY_BRACKET)
			{
				final DirectiveList body = new DirectiveList();
				this.directive.setBody(body);
				pm.pushParser(new BlockParser(body));
				this.mode = BODY_END;
				return;
			}

			this.list.add(this.directive);
			pm.popParser(true);
			return;
		case CLOSE_PAREN:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "directive.close_paren");
				return;
			}

			this.mode = BODY;
			return;
		case BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;
			this.list.add(this.directive);
			pm.popParser();
		}
	}
}

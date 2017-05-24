package dyvil.tools.gensrc.parser;

import dyvil.tools.gensrc.ast.directive.DirectiveList;
import dyvil.tools.gensrc.ast.directive.IfDirective;
import dyvil.tools.gensrc.lexer.GenSrcSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.token.IToken;

public class IfDirectiveParser extends Parser
{
	public static final int IF            = 0;
	public static final int OPEN_PAREN    = 1;
	public static final int CLOSE_PAREN   = 2;
	public static final int THEN_BODY     = 3;
	public static final int THEN_BODY_END = 4;
	public static final int ELSE          = 5;
	public static final int ELSE_BODY     = 6;
	public static final int ELSE_BODY_END = 7;

	private final DirectiveList list;

	private IfDirective directive;

	public IfDirectiveParser(DirectiveList list)
	{
		this.list = list;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		// #if (EXPRESSION) {BLOCK}
		// #if (EXPRESSION) {BLOCK} #else {BLOCK}

		final int type = token.type();
		switch (this.mode)
		{
		case IF:
			assert type == GenSrcSymbols.IF;
			this.directive = new IfDirective(token.raw());
			this.mode = OPEN_PAREN;
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.report(token, "directive.if.open_paren");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			pm.pushParser(new ExpressionParser(this.directive::setCondition));
			this.mode = CLOSE_PAREN;
			return;
		case CLOSE_PAREN:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "directive.if.close_paren");
				return;
			}

			this.mode = THEN_BODY;
			return;
		case THEN_BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "directive.if.open_brace");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			final DirectiveList thenBlock = new DirectiveList();
			pm.pushParser(new BlockParser(thenBlock));
			this.directive.setThenBlock(thenBlock);
			this.mode = THEN_BODY_END;
			return;
		case THEN_BODY_END:
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.report(token, "directive.if.close_brace");
				return;
			}

			this.mode = ELSE;
			return;
		case ELSE:
			if (type != BaseSymbols.HASH || token.next().type() != GenSrcSymbols.ELSE)
			{
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			this.mode = ELSE_BODY;
			return;
		case ELSE_BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "directive.if.else.open_brace");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			final DirectiveList elseBlock = new DirectiveList();
			pm.pushParser(new BlockParser(elseBlock));
			this.directive.setElseBlock(elseBlock);
			this.mode = ELSE_BODY_END;
			return;
		case ELSE_BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;

			this.list.add(this.directive);
			pm.popParser();
		}
	}
}

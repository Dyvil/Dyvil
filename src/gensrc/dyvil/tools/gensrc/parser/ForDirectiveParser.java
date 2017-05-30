package dyvil.tools.gensrc.parser;

import dyvil.tools.gensrc.ast.directive.DirectiveList;
import dyvil.tools.gensrc.ast.directive.ForDirective;
import dyvil.tools.gensrc.ast.expression.RangeOperator;
import dyvil.tools.gensrc.lexer.GenSrcSymbols;
import dyvil.tools.gensrc.parser.expression.ExpressionParser;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;

public class ForDirectiveParser extends Parser
{
	public static final int FOR         = 0;
	public static final int OPEN_PAREN  = 1;
	public static final int VAR_NAME    = 2;
	public static final int ARROW       = 3;
	public static final int CLOSE_PAREN = 4;
	public static final int BODY        = 5;
	public static final int BODY_END    = 6;

	private final DirectiveList list;

	private ForDirective directive;

	public ForDirectiveParser(DirectiveList list)
	{
		this.list = list;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		// #for (IDENTIFIER <- EXPRESSION) {BLOCK}

		final int type = token.type();
		switch (this.mode)
		{
		case FOR:
			assert type == GenSrcSymbols.FOR;
			this.mode = OPEN_PAREN;
			this.directive = new ForDirective(token.raw());
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.report(token, "directive.for.open_paren");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			this.mode = VAR_NAME;
			return;
		case VAR_NAME:
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				this.directive.setVarName(token.nameValue());
				this.mode = ARROW;
				return;
			}

			pm.report(token, "directive.for.identifier");
			if (type == GenSrcSymbols.ARROW_LEFT || type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				this.mode = ARROW;
			}
			return;
		case ARROW:
			if (type != GenSrcSymbols.ARROW_LEFT)
			{
				pm.reparse();
				pm.report(token, "directive.for.arrow_left");
			}

			pm.pushParser(new ExpressionParser(this.directive::setIterable));
			this.mode = CLOSE_PAREN;
			return;
		case CLOSE_PAREN:
			if (type == Tokens.SYMBOL_IDENTIFIER && token.nameValue().unqualified.equals(".."))
			{
				final RangeOperator rangeOp = new RangeOperator(token.raw());
				rangeOp.setStart(this.directive.getIterable());
				pm.pushParser(new ExpressionParser(rangeOp::setEnd));
				this.directive.setIterable(rangeOp);
				return;
			}

			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "directive.for.close_paren");
				return;
			}

			this.mode = BODY;
			return;
		case BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "directive.for.open_brace");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			final DirectiveList body = new DirectiveList();
			pm.pushParser(new BlockParser(body));
			this.directive.setBlock(body);
			this.mode = BODY_END;
			return;
		case BODY_END:
			assert type == BaseSymbols.CLOSE_CURLY_BRACKET;
			this.list.add(this.directive);
			pm.popParser();
		}
	}
}

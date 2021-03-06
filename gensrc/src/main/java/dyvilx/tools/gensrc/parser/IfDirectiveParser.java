package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.gensrc.ast.directive.IfDirective;
import dyvilx.tools.gensrc.lexer.GenSrcLexer;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

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

	private final StatementList list;

	private IfDirective directive;

	public IfDirectiveParser(StatementList list)
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
				pm.report(token, "if.open_paren");
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
				pm.report(token, "if.close_paren");
				return;
			}

			this.mode = THEN_BODY;
			return;
		case THEN_BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "if.open_brace");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			final StatementList thenBlock = new StatementList();
			pm.pushParser(new BlockParser(thenBlock));
			this.directive.setThen(thenBlock);
			this.mode = THEN_BODY_END;
			return;
		case THEN_BODY_END:
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.report(token, "if.close_brace");
				this.list.add(this.directive);
				pm.popParser();
				return;
			}

			this.mode = ELSE;
			return;
		case ELSE:
			if (type == Tokens.STRING && isBlank(token.stringValue()) && isHashElse(token.next()))
			{
				// ignore empty whitespace between } and #else
				pm.skip(2); // skip the '#' and 'else'
				this.mode = ELSE_BODY;
				return;
			}

			if (isHashElse(token))
			{
				pm.skip(); // skip the 'else'
				this.mode = ELSE_BODY;
				return;
			}

			this.list.add(this.directive);
			pm.popParser(true);
			return;
		case ELSE_BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "if.else.open_brace");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			final StatementList elseBlock = new StatementList();
			pm.pushParser(new BlockParser(elseBlock));
			this.directive.setElse(elseBlock);
			this.mode = ELSE_BODY_END;
			return;
		case ELSE_BODY_END:
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.report(token, "if.else.close_brace");
			}
			this.list.add(this.directive);
			pm.popParser();
		}
	}

	private static boolean isHashElse(IToken token)
	{
		return token.type() == BaseSymbols.HASH && token.next().type() == GenSrcSymbols.ELSE;
	}

	private static boolean isBlank(String value)
	{
		final int length = value.length();
		return GenSrcLexer.skipWhitespace(value, 0, length) == length;
	}
}

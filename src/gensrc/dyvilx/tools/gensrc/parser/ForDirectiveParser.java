package dyvilx.tools.gensrc.parser;

import dyvilx.tools.compiler.ast.field.Variable;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.type.TypeParser;
import dyvilx.tools.gensrc.ast.directive.ForEachDirective;
import dyvilx.tools.gensrc.lexer.GenSrcSymbols;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

public class ForDirectiveParser extends Parser
{
	protected static final int FOR             = 0;
	protected static final int OPEN_PAREN      = 1;
	protected static final int VAR_NAME        = 2;
	protected static final int TYPE_ASCRIPTION = 3;
	protected static final int ARROW           = 4;
	protected static final int CLOSE_PAREN     = 5;
	protected static final int BODY            = 6;
	protected static final int BODY_END        = 7;

	private final StatementList list;

	private ForEachDirective directive;

	public ForDirectiveParser(StatementList list)
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
			this.directive = new ForEachDirective(token.raw(), null);
			return;
		case OPEN_PAREN:
			if (type != BaseSymbols.OPEN_PARENTHESIS)
			{
				pm.report(token, "for.open_paren");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			this.mode = VAR_NAME;
			return;
		case VAR_NAME:
			if (type == Tokens.LETTER_IDENTIFIER)
			{
				this.directive.setVariable(new Variable(token.raw(), token.nameValue(), Types.UNKNOWN));
				this.mode = TYPE_ASCRIPTION;
				return;
			}

			pm.report(token, "for.identifier");
			if (type == BaseSymbols.CLOSE_PARENTHESIS)
			{
				this.mode = BODY;
			}
			return;
		case TYPE_ASCRIPTION:
			if (type == BaseSymbols.COLON)
			{
				pm.pushParser(new TypeParser(this.directive.getVariable()::setType));
				this.mode = ARROW;
				return;
			}
			// Fallthrough
		case ARROW:
			if (type != GenSrcSymbols.ARROW_LEFT)
			{
				pm.reparse();
				pm.report(token, "for.arrow_left");
			}

			pm.pushParser(new ExpressionParser(this.directive.getVariable()));
			this.mode = CLOSE_PAREN;
			return;
		case CLOSE_PAREN:
			if (type != BaseSymbols.CLOSE_PARENTHESIS)
			{
				pm.report(token, "for.close_paren");
				return;
			}

			this.mode = BODY;
			return;
		case BODY:
			if (type != BaseSymbols.OPEN_CURLY_BRACKET)
			{
				pm.report(token, "for.open_brace");
				this.list.add(this.directive);
				pm.popParser(true);
				return;
			}

			final StatementList body = new StatementList();
			pm.pushParser(new BlockParser(body));
			this.directive.setAction(body);
			this.mode = BODY_END;
			return;
		case BODY_END:
			if (type != BaseSymbols.CLOSE_CURLY_BRACKET)
			{
				pm.report(token, "for.close_brace");
			}
			this.list.add(this.directive);
			pm.popParser();
		}
	}
}

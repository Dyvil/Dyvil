package dyvilx.tools.repl.input;

import dyvilx.tools.compiler.parser.DyvilSymbols;
import dyvilx.tools.compiler.parser.classes.MemberParser;
import dyvilx.tools.compiler.parser.expression.ExpressionParser;
import dyvilx.tools.compiler.parser.header.SourceFileParser;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.TryParserManager;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;
import dyvilx.tools.repl.context.REPLContext;

import static dyvilx.tools.compiler.parser.header.SourceFileParser.NO_CLASSES;
import static dyvilx.tools.compiler.parser.header.SourceFileParser.ONE_ELEMENT;
import static dyvilx.tools.parsing.TryParserManager.EXIT_ON_ROOT;

public class REPLParser extends Parser
{
	protected static final int ELEMENT   = 0;
	protected static final int SEPARATOR = 1;

	protected static final TryParserManager TRY_PARSER = new TryParserManager(DyvilSymbols.INSTANCE);

	protected final REPLContext context;

	public REPLParser(REPLContext context)
	{
		this.context = context;
		this.reset();
	}

	public void reset()
	{
		this.mode = ELEMENT;
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (type == Tokens.EOF)
		{
			pm.popParser();
			return;
		}

		switch (this.mode)
		{
		case ELEMENT:

			this.mode = SEPARATOR;
			if (TRY_PARSER.tryParse(pm, new SourceFileParser(this.context).withFlags(ONE_ELEMENT | NO_CLASSES), token,
			                        EXIT_ON_ROOT))
			{
				return;
			}

			if (TRY_PARSER.tryParse(pm, new MemberParser<>(this.context), token, EXIT_ON_ROOT))
			{
				return;
			}

			pm.pushParser(new ExpressionParser(this.context::addValue));
			return;
		case SEPARATOR:
			this.mode = ELEMENT;
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
			case BaseSymbols.COMMA:
				return;
			}
			pm.report(token, "statement_list.semicolon");
		}
	}
}

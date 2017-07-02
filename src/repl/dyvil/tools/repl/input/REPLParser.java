package dyvil.tools.repl.input;

import dyvil.tools.compiler.parser.classes.MemberParser;
import dyvil.tools.compiler.parser.expression.ExpressionParser;
import dyvil.tools.compiler.parser.header.DyvilHeaderParser;
import dyvil.tools.compiler.transform.DyvilSymbols;
import dyvil.tools.parsing.IParserManager;
import dyvil.tools.parsing.Parser;
import dyvil.tools.parsing.TryParserManager;
import dyvil.tools.parsing.lexer.BaseSymbols;
import dyvil.tools.parsing.lexer.Tokens;
import dyvil.tools.parsing.token.IToken;
import dyvil.tools.repl.context.REPLContext;

import static dyvil.tools.compiler.parser.header.DyvilHeaderParser.ONE_ELEMENT;
import static dyvil.tools.parsing.TryParserManager.EXIT_ON_ROOT;

public class REPLParser extends Parser
{
	protected static final int ELEMENT   = 0;
	protected static final int SEPARATOR = 1;

	protected static final TryParserManager TRY_PARSER   = new TryParserManager(DyvilSymbols.INSTANCE);

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
			if (TRY_PARSER
				    .tryParse(pm, new DyvilHeaderParser(this.context).withFlags(ONE_ELEMENT), token, EXIT_ON_ROOT))
			{
				return;
			}

			if (TRY_PARSER.tryParse(pm, new MemberParser<>(this.context), token, EXIT_ON_ROOT))
			{
				return;
			}

			pm.pushParser(new ExpressionParser(this.context));
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

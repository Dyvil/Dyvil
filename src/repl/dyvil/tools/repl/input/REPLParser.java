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

import static dyvil.tools.compiler.parser.classes.MemberParser.NO_UNINITIALIZED_VARIABLES;
import static dyvil.tools.compiler.parser.classes.MemberParser.OPERATOR_ERROR;
import static dyvil.tools.compiler.parser.header.DyvilHeaderParser.ONE_ELEMENT;
import static dyvil.tools.parsing.TryParserManager.EXIT_ON_ROOT;

public class REPLParser extends Parser
{
	protected static final int ELEMENT   = 0;
	protected static final int SEPARATOR = 1;

	protected static final TryParserManager TRY_PARSER   = new TryParserManager(DyvilSymbols.INSTANCE);

	/**
	 * Flags to use for the Member Parser.
	 * It should not parse uninitialized variables as such, and create an error instead of a warning for invalid
	 * operator methods.
	 */
	private static final   int              MEMBER_FLAGS = NO_UNINITIALIZED_VARIABLES | OPERATOR_ERROR;

	/**
	 * Flags to use for the Header Element Parser. Uses ONE_ELEMENT to make it doesn't consume semicolons.
	 */
	private static final   int              HEADER_FLAGS = ONE_ELEMENT;

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
		switch (this.mode)
		{
		case ELEMENT:
			if (type == Tokens.EOF)
			{
				pm.popParser();
				return;
			}

			this.mode = SEPARATOR;
			if (TRY_PARSER
				    .tryParse(pm, new DyvilHeaderParser(this.context).withFlags(HEADER_FLAGS), token, EXIT_ON_ROOT))
			{
				return;
			}

			if (TRY_PARSER.tryParse(pm, new MemberParser<>(this.context).withFlag(MEMBER_FLAGS), token, EXIT_ON_ROOT))
			{
				return;
			}

			pm.pushParser(new ExpressionParser(this.context));
			return;
		case SEPARATOR:
			switch (type)
			{
			case Tokens.EOF:
				pm.popParser();
				return;
			case BaseSymbols.SEMICOLON:
				this.mode = ELEMENT;
				return;
			}

			this.mode = ELEMENT;
			pm.report(token, "statement_list.semicolon");
			pm.reparse();
		}
	}
}

package dyvilx.tools.compiler.parser.pattern;

import dyvilx.tools.compiler.ast.pattern.Pattern;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class PatternListParser extends Parser
{
	// =============== Constants ===============

	private static final int PATTERN = 0;
	private static final int COMMA   = 1;

	// =============== Fields ===============

	protected final Consumer<Pattern> consumer;

	// =============== Constructors ===============

	public PatternListParser(Consumer<Pattern> consumer)
	{
		this.consumer = consumer;
		// this.mode = PATTERN;
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (BaseSymbols.isCloseBracket(type))
		{
			pm.popParser(true);
			return;
		}

		switch (this.mode)
		{
		case PATTERN:
			this.mode = COMMA;
			pm.pushParser(new PatternParser(this.consumer), true);
			return;
		case COMMA:
			switch (type)
			{
			case BaseSymbols.SEMICOLON:
				if (!token.isInferred())
				{
					pm.report(token, "pattern.list.comma");
				}
				// Fallthrough
			case BaseSymbols.COMMA:
				this.mode = PATTERN;
				return;
			default:
				pm.report(token, "pattern.list.comma");
				return;
			}
		}
	}
}

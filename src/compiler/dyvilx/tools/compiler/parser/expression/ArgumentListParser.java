package dyvilx.tools.compiler.parser.expression;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class ArgumentListParser extends Parser
{
	// =============== Constants ===============

	private static final int NAME      = 0;
	private static final int VALUE     = 1;
	private static final int SEPARATOR = 2;

	// =============== Instance Fields ===============

	// --------------- Constructor Fields ---------------

	private final Consumer<ArgumentList> consumer;

	// --------------- Temporary Fields ---------------

	private Name label;

	private ArgumentList arguments = new ArgumentList();

	// =============== Constructors ===============

	public ArgumentListParser(Consumer<ArgumentList> consumer)
	{
		this.consumer = consumer;
		// this.mode = NAME
	}

	// =============== Static Methods ===============

	public static void parseArguments(IParserManager pm, IToken next, Consumer<ArgumentList> consumer)
	{
		final int nextType = next.type();

		if (BaseSymbols.isCloseBracket(nextType))
		{
			consumer.accept(ArgumentList.empty());
			return;
		}

		pm.pushParser(new ArgumentListParser(consumer));
	}

	// =============== Methods ===============

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (BaseSymbols.isCloseBracket(type) || type == Tokens.EOF)
		{
			if (this.label != null)
			{
				pm.report(token, "arguments.expression");
			}

			this.end(pm);
			return;
		}

		switch (this.mode)
		{
		case NAME:
			this.mode = VALUE;
			this.label = null;
			if (token.next().type() == BaseSymbols.COLON)
			{
				if (type == BaseSymbols.UNDERSCORE)
				{
					// _ : ...
					this.label = ArgumentList.FENCE;
					pm.skip();
					return;
				}
				else if (Tokens.isIdentifier(type))
				{
					this.label = token.nameValue();
					pm.skip();
					return;
				}
				else if (Tokens.isKeyword(type)) // TODO do not accept modifier keywords
				{
					this.label = Name.fromRaw(token.stringValue());
					pm.skip();
					return;
				}
			}
			else if (type == Tokens.SYMBOL_IDENTIFIER && token.nameValue().unqualified.equals("_:"))
			{
				// _: ...
				this.label = ArgumentList.FENCE;
				return;
			}
			// Fallthrough
		case VALUE:
			this.mode = SEPARATOR;
			pm.pushParser(new ExpressionParser(value -> this.arguments.add(this.label, value)), true);
			return;
		case SEPARATOR:
			this.mode = NAME;
			if (type != BaseSymbols.COMMA && type != BaseSymbols.SEMICOLON)
			{
				pm.reparse();
				pm.report(token, "arguments.separator");
			}
		}
	}

	private void end(IParserManager pm)
	{
		this.consumer.accept(this.arguments);

		pm.popParser(true);
	}
}

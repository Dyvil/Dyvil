package dyvilx.tools.compiler.parser.type;

import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.parsing.IParserManager;
import dyvilx.tools.parsing.Parser;
import dyvilx.tools.parsing.lexer.BaseSymbols;
import dyvilx.tools.parsing.lexer.Tokens;
import dyvilx.tools.parsing.token.IToken;

import java.util.function.Consumer;

public class TypeListParser extends Parser
{
	// =============== Constants ===============

	private static final int TYPE      = 0;
	private static final int SEPARATOR = 1;

	// =============== Fields ===============

	protected Consumer<IType> consumer;

	private boolean closeAngle;

	// =============== Constructors ===============

	public TypeListParser(Consumer<IType> consumer)
	{
		this.consumer = consumer;
		// this.mode = TYPE;
	}

	public TypeListParser(Consumer<IType> consumer, boolean closeAngle)
	{
		this.consumer = consumer;
		this.closeAngle = closeAngle;
		// this.mode = TYPE;
	}

	// =============== Methods ===============

	private boolean isEndToken(IToken token, int type)
	{
		switch (type)
		{
		case BaseSymbols.OPEN_CURLY_BRACKET:
		case BaseSymbols.EQUALS:
		case BaseSymbols.SEMICOLON:
		case Tokens.EOF:
			return true;
		}
		return BaseSymbols.isCloseBracket(type) || this.closeAngle && TypeParser.isGenericEnd(token, type);
	}

	@Override
	public void parse(IParserManager pm, IToken token)
	{
		final int type = token.type();
		if (this.isEndToken(token, type))
		{
			pm.popParser(true);
			return;
		}

		switch (this.mode)
		{
		case TYPE:
			this.mode = SEPARATOR;
			// uses optional flag to allow trailing commas
			pm.pushParser(new TypeParser(this.consumer, this.closeAngle).withFlags(TypeParser.OPTIONAL), true);
			return;
		case SEPARATOR:
			switch (type)
			{
			case BaseSymbols.COMMA:
				this.mode = TYPE;
				return;
			default:
				pm.report(token, "type.list.comma");
				return;
			}
		}
	}
}

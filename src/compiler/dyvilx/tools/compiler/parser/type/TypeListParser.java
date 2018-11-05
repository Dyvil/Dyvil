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

	// --------------- Parse Modes ---------------

	private static final int TYPE      = 0;
	private static final int SEPARATOR = 1;

	// --------------- Flags ---------------

	public static final int CLOSE_ANGLE    = 1;
	public static final int ANY_TERMINATOR = 2;

	// =============== Fields ===============

	protected Consumer<IType> consumer;

	private byte flags;

	// =============== Constructors ===============

	public TypeListParser(Consumer<IType> consumer)
	{
		this.consumer = consumer;
		// this.mode = TYPE;
	}

	// =============== Properties ===============

	public TypeListParser withFlags(int flags)
	{
		this.flags |= flags;
		return this;
	}

	private boolean allowsCloseAngle()
	{
		return (this.flags & CLOSE_ANGLE) != 0;
	}

	private boolean allowsAnyTerminator()
	{
		return (this.flags & ANY_TERMINATOR) != 0;
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
		return BaseSymbols.isCloseBracket(type) //
		       || this.allowsCloseAngle() && TypeParser.isGenericEnd(token, type);
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
			pm.pushParser(new TypeParser(this.consumer, this.allowsCloseAngle()), true);
			return;
		case SEPARATOR:
			switch (type)
			{
			case BaseSymbols.COMMA:
				this.mode = TYPE;
				return;
			default:
				if (this.allowsAnyTerminator())
				{
					pm.popParser(true);
					return;
				}

				pm.report(token, "type.list.comma");
				return;
			}
		}
	}
}

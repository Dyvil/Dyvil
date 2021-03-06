package dyvilx.tools.parsing.lexer;

public class BaseSymbols implements Symbols
{
	public static final BaseSymbols INSTANCE = new BaseSymbols();

	protected static final int PARENTHESIS = Tokens.BRACKET | 0x00010000;
	protected static final int SQUARE      = Tokens.BRACKET | 0x00020000;
	protected static final int CURLY       = Tokens.BRACKET | 0x00040000;

	public static final int OPEN_BRACKET  = Tokens.BRACKET;
	public static final int CLOSE_BRACKET = Tokens.BRACKET | 0x00100000;

	public static final int OPEN_PARENTHESIS     = OPEN_BRACKET | PARENTHESIS;
	public static final int CLOSE_PARENTHESIS    = CLOSE_BRACKET | PARENTHESIS;
	public static final int OPEN_SQUARE_BRACKET  = OPEN_BRACKET | SQUARE;
	public static final int CLOSE_SQUARE_BRACKET = CLOSE_BRACKET | SQUARE;
	public static final int OPEN_CURLY_BRACKET   = OPEN_BRACKET | CURLY;
	public static final int CLOSE_CURLY_BRACKET  = CLOSE_BRACKET | CURLY;

	public static final int DOT        = Tokens.SYMBOL | 0x00010000;
	public static final int COLON      = Tokens.SYMBOL | 0x00020000;
	public static final int SEMICOLON  = Tokens.SYMBOL | 0x00030000;
	public static final int COMMA      = Tokens.SYMBOL | 0x00040000;
	public static final int EQUALS     = Tokens.SYMBOL | 0x00050000;
	public static final int HASH       = Tokens.SYMBOL | 0x00060000;
	public static final int UNDERSCORE = Tokens.SYMBOL | 0x00070000;

	@Override
	public int getKeywordType(String value)
	{
		return 0;
	}

	@Override
	public int getSymbolType(String value)
	{
		switch (value)
		{
		case ":":
			return BaseSymbols.COLON;
		case "=":
			return BaseSymbols.EQUALS;
		case "#":
			return BaseSymbols.HASH;
		case "_":
			return BaseSymbols.UNDERSCORE;
		}
		return 0;
	}

	@Override
	public String toString(int type)
	{
		switch (type)
		{
		case BaseSymbols.DOT:
			return ".";
		case BaseSymbols.COLON:
			return ":";
		case BaseSymbols.SEMICOLON:
			return ";";
		case BaseSymbols.COMMA:
			return ",";
		case BaseSymbols.EQUALS:
			return "=";
		case BaseSymbols.HASH:
			return "#";
		case BaseSymbols.UNDERSCORE:
			return "_";
		case BaseSymbols.OPEN_PARENTHESIS:
			return "(";
		case BaseSymbols.CLOSE_PARENTHESIS:
			return ")";
		case BaseSymbols.OPEN_SQUARE_BRACKET:
			return "[";
		case BaseSymbols.CLOSE_SQUARE_BRACKET:
			return "]";
		case BaseSymbols.OPEN_CURLY_BRACKET:
			return "{";
		case BaseSymbols.CLOSE_CURLY_BRACKET:
			return "}";
		}
		return null;
	}

	@Override
	public int getLength(int type)
	{
		return 1;
	}

	public static boolean isCloseBracket(int type)
	{
		return (type & CLOSE_BRACKET) == CLOSE_BRACKET;
	}

	public static boolean isTerminator(int type)
	{
		switch (type)
		{
		case Tokens.EOF:
		case COMMA:
		case SEMICOLON:
		case COLON:
		case CLOSE_CURLY_BRACKET:
		case CLOSE_PARENTHESIS:
		case CLOSE_SQUARE_BRACKET:
			return true;
		}
		return false;
	}
}

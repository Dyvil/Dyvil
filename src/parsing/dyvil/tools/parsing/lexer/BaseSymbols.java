package dyvil.tools.parsing.lexer;

public class BaseSymbols implements Symbols
{
	public static final BaseSymbols	INSTANCE				= new BaseSymbols();
	
	public static final int			PARENTHESIS				= Tokens.BRACKET | 0x00010000;
	public static final int			SQUARE					= Tokens.BRACKET | 0x00020000;
	public static final int			CURLY					= Tokens.BRACKET | 0x00040000;
	
	public static final int			OPEN					= 0x00000000;
	public static final int			CLOSE					= 0x00100000;
	
	public static final int			OPEN_BRACKET			= Tokens.BRACKET | OPEN;
	public static final int			CLOSE_BRACKET			= Tokens.BRACKET | CLOSE;
	public static final int			OPEN_PARENTHESIS		= PARENTHESIS | OPEN;
	public static final int			CLOSE_PARENTHESIS		= PARENTHESIS | CLOSE;
	public static final int			OPEN_SQUARE_BRACKET		= SQUARE | OPEN;
	public static final int			CLOSE_SQUARE_BRACKET	= SQUARE | CLOSE;
	public static final int			OPEN_CURLY_BRACKET		= CURLY | OPEN;
	public static final int			CLOSE_CURLY_BRACKET		= CURLY | CLOSE;
	
	public static final int			DOT						= Tokens.SYMBOL | 0x00010000;
	public static final int			COLON					= Tokens.SYMBOL | 0x00020000;
	public static final int			SEMICOLON				= Tokens.SYMBOL | 0x00030000;
	public static final int			COMMA					= Tokens.SYMBOL | 0x00040000;
	public static final int			EQUALS					= Tokens.SYMBOL | 0x00050000;
	
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
	
}

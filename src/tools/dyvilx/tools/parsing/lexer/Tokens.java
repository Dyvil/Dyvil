package dyvilx.tools.parsing.lexer;

public interface Tokens
{
	int EOF = 0;

	// IDENTIFIERS
	int MOD_LETTER = 0x00010000;
	int MOD_SYMBOL = 0x00020000;
	int MOD_DOT    = 0x00040000;

	int IDENTIFIER         = 0x00000001;
	int LETTER_IDENTIFIER  = IDENTIFIER | MOD_LETTER;
	int SYMBOL_IDENTIFIER  = IDENTIFIER | MOD_SYMBOL;
	int SPECIAL_IDENTIFIER = 0x00001001;

	int KEYWORD = 0x00000002;
	int SYMBOL  = 0x00000004;
	int BRACKET = 0x00000008;

	int INT    = 0x00000010;
	int LONG   = 0x00000020;
	int FLOAT  = 0x00000040;
	int DOUBLE = 0x00000080;

	// STRINGS
	int STRING               = 0x00000100;
	int STRING_START         = STRING | 0x00010000;
	int STRING_PART          = STRING | 0x00020000;
	int STRING_END           = STRING | 0x00040000;
	int SINGLE_QUOTED_STRING = 0x00000400;
	int VERBATIM_STRING      = 0x00000800;
	int VERBATIM_CHAR        = 0x00001000;

	static boolean isIdentifier(int type)
	{
		return (type & IDENTIFIER) != 0;
	}

	static boolean isKeyword(int type)
	{
		return (type & KEYWORD) != 0;
	}

	static boolean isSymbolic(int type)
	{
		return type == SYMBOL_IDENTIFIER || (type & SYMBOL) != 0;
	}
}

package dyvil.tools.compiler.util;

public interface Tokens
{
	// TYPES
	public int	TYPE_IDENTIFIER			= 0x00000001;
	public int	TYPE_KEYWORD			= 0x00000002;
	public int	TYPE_SYMBOL				= 0x00000004;
	public int	TYPE_BRACKET			= 0x00000008;
	
	public int	TYPE_INT				= 0x00000010;
	public int	TYPE_LONG				= 0x00000020;
	public int	TYPE_FLOAT				= 0x00000040;
	public int	TYPE_DOUBLE				= 0x00000080;
	
	public int	TYPE_STRING				= 0x00000100;
	public int	TYPE_STRING_2			= 0x00000101;
	
	public int	TYPE_CHAR				= 0x00000200;
	
	public int	TYPE_COMMENT			= 0x00001000;
	
	// IDENTIFIERS
	public int	MOD_LETTER				= 0x00010000;
	public int	MOD_SYMBOL				= 0x00020000;
	public int	MOD_DOTS				= 0x00040000;
	
	public int	TYPE_LETTER_ID			= TYPE_IDENTIFIER | MOD_LETTER;
	public int	TYPE_SYMBOL_ID			= TYPE_IDENTIFIER | MOD_SYMBOL;
	
	// KEYWORDS
	public int	WILDCARD				= TYPE_KEYWORD | 0x00010000;
	public int	AT						= TYPE_KEYWORD | 0x00020000;
	public int	NULL					= TYPE_KEYWORD | 0x00030000;
	public int	TRUE					= TYPE_KEYWORD | 0x00040000;
	public int	FALSE					= TYPE_KEYWORD | 0x00050000;
	public int	THIS					= TYPE_KEYWORD | 0x00060000;
	public int	SUPER					= TYPE_KEYWORD | 0x00070000;
	public int	NEW						= TYPE_KEYWORD | 0x00080000;
	public int	RETURN					= TYPE_KEYWORD | 0x00090000;
	public int	IF						= TYPE_KEYWORD | 0x000A0000;
	public int	ELSE					= TYPE_KEYWORD | 0x000B0000;
	public int	WHILE					= TYPE_KEYWORD | 0x000C0000;
	public int	DO						= TYPE_KEYWORD | 0x000D0000;
	public int	FOR						= TYPE_KEYWORD | 0x000E0000;
	public int	SWITCH					= TYPE_KEYWORD | 0x000F0000;
	public int	CASE					= TYPE_KEYWORD | 0x00100000;
	public int	TRY						= TYPE_KEYWORD | 0x00200000;
	public int	CATCH					= TYPE_KEYWORD | 0x00300000;
	public int	FINALLY					= TYPE_KEYWORD | 0x00400000;
	
	// BRACKETS
	public int	MOD_PARENTHESIS			= 0x00010000;
	public int	MOD_SQUARE				= 0x00020000;
	public int	MOD_CURLY				= 0x00040000;
	public int	MOD_OPEN				= 0x00000000;
	public int	MOD_CLOSE				= 0x00100000;
	
	public int	OPEN_BRACKET			= TYPE_BRACKET | MOD_OPEN;
	public int	CLOSE_BRACKET			= TYPE_BRACKET | MOD_CLOSE;
	public int	OPEN_PARENTHESIS		= TYPE_BRACKET | MOD_PARENTHESIS | MOD_OPEN;
	public int	CLOSE_PARENTHESIS		= TYPE_BRACKET | MOD_PARENTHESIS | MOD_CLOSE;
	public int	OPEN_SQUARE_BRACKET		= TYPE_BRACKET | MOD_SQUARE | MOD_OPEN;
	public int	CLOSE_SQUARE_BRACKET	= TYPE_BRACKET | MOD_SQUARE | MOD_CLOSE;
	public int	OPEN_CURLY_BRACKET		= TYPE_BRACKET | MOD_CURLY | MOD_OPEN;
	public int	CLOSE_CURLY_BRACKET		= TYPE_BRACKET | MOD_CURLY | MOD_CLOSE;
	
	// SYMBOLS
	public int	DOT						= TYPE_SYMBOL | 0x00010000;
	public int	COLON					= TYPE_SYMBOL | 0x00020000;
	public int	SEMICOLON				= TYPE_SYMBOL | 0x00030000;
	public int	COMMA					= TYPE_SYMBOL | 0x00040000;
	public int	EQUALS					= TYPE_SYMBOL | 0x00050000;
	public int	ARROW_OPERATOR			= TYPE_SYMBOL | 0x00060000;
	
	// COMMENTS
	public int	MOD_LINE				= 0x00010000;
	public int	MOD_BLOCK				= 0x00020000;
	
	public int	LINE_COMMENT			= TYPE_COMMENT | MOD_LINE;
	public int	BLOCK_COMMENT			= TYPE_COMMENT | MOD_BLOCK;
	
	// NUMBERS
	public int	MOD_DEC					= 0x00000000;
	public int	MOD_BIN					= 0x00010000;
	public int	MOD_OCT					= 0x00020000;
	public int	MOD_HEX					= MOD_BIN | MOD_OCT;
}

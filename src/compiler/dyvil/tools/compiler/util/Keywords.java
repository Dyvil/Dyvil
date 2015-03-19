package dyvil.tools.compiler.util;

public interface Keywords
{
	int	NULL					= Tokens.KEYWORD | 0x00010000;
	int	TRUE					= Tokens.KEYWORD | 0x00020000;
	int	FALSE					= Tokens.KEYWORD | 0x00030000;
	int	THIS					= Tokens.KEYWORD | 0x00040000;
	int	SUPER					= Tokens.KEYWORD | 0x00050000;
	int	NEW						= Tokens.KEYWORD | 0x00060000;
	int	RETURN					= Tokens.KEYWORD | 0x00070000;
	int	IF						= Tokens.KEYWORD | 0x00080000;
	int	ELSE					= Tokens.KEYWORD | 0x00090000;
	int	WHILE					= Tokens.KEYWORD | 0x000A0000;
	int	DO						= Tokens.KEYWORD | 0x000B0000;
	int	FOR						= Tokens.KEYWORD | 0x000C0000;
	int	BREAK					= Tokens.KEYWORD | 0x000D0000;
	int	CONTINUE				= Tokens.KEYWORD | 0x000E0000;
	int	GOTO					= Tokens.KEYWORD | 0x000F0000;
	int	CASE					= Tokens.KEYWORD | 0x00100000;
	int	TRY						= Tokens.KEYWORD | 0x00110000;
	int	CATCH					= Tokens.KEYWORD | 0x00120000;
	int	FINALLY					= Tokens.KEYWORD | 0x00130000;
	int	THROW					= Tokens.KEYWORD | 0x00140000;
	int	SYNCHRONIZED			= Tokens.KEYWORD | 0x00150000;

	public static int getKeywordType(String s)
	{
		switch (s)
		{
		case "null":
			return Keywords.NULL;
		case "true":
			return Keywords.TRUE;
		case "false":
			return Keywords.FALSE;
		case "this":
			return Keywords.THIS;
		case "super":
			return Keywords.SUPER;
		case "new":
			return Keywords.NEW;
		case "return":
			return Keywords.RETURN;
		case "if":
			return Keywords.IF;
		case "else":
			return Keywords.ELSE;
		case "while":
			return Keywords.WHILE;
		case "do":
			return Keywords.DO;
		case "for":
			return Keywords.FOR;
		case "break":
			return Keywords.BREAK;
		case "continue":
			return Keywords.CONTINUE;
		case "goto":
			return Keywords.GOTO;
		case "case":
			return Keywords.CASE;
		case "try":
			return Keywords.TRY;
		case "catch":
			return Keywords.CATCH;
		case "finally":
			return Keywords.FINALLY;
		case "synchronized":
			return Keywords.SYNCHRONIZED;
		case "throw":
			return Keywords.THROW;
		}
		return 0;
	}
	
	public static String keywordToString(int type)
	{
		// FIXME
		return "";
	}
}

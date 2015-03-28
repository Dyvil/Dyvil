package dyvil.tools.compiler.transform;


public interface Keywords
{
	int	NULL			= Tokens.KEYWORD | 0x00010000;
	int	TRUE			= Tokens.KEYWORD | 0x00020000;
	int	FALSE			= Tokens.KEYWORD | 0x00030000;
	int	THIS			= Tokens.KEYWORD | 0x00040000;
	int	SUPER			= Tokens.KEYWORD | 0x00050000;
	int	NEW				= Tokens.KEYWORD | 0x00060000;
	int	RETURN			= Tokens.KEYWORD | 0x00070000;
	int	IF				= Tokens.KEYWORD | 0x00080000;
	int	ELSE			= Tokens.KEYWORD | 0x00090000;
	int	WHILE			= Tokens.KEYWORD | 0x000A0000;
	int	DO				= Tokens.KEYWORD | 0x000B0000;
	int	FOR				= Tokens.KEYWORD | 0x000C0000;
	int	BREAK			= Tokens.KEYWORD | 0x000D0000;
	int	CONTINUE		= Tokens.KEYWORD | 0x000E0000;
	int	GOTO			= Tokens.KEYWORD | 0x000F0000;
	int	CASE			= Tokens.KEYWORD | 0x00100000;
	int	TRY				= Tokens.KEYWORD | 0x00110000;
	int	CATCH			= Tokens.KEYWORD | 0x00120000;
	int	FINALLY			= Tokens.KEYWORD | 0x00130000;
	int	THROW			= Tokens.KEYWORD | 0x00140000;
	int	SYNCHRONIZED	= Tokens.KEYWORD | 0x00150000;
	
	public static int getKeywordType(String s)
	{
		switch (s)
		{
		case "null":
			return NULL;
		case "true":
			return TRUE;
		case "false":
			return FALSE;
		case "this":
			return THIS;
		case "super":
			return SUPER;
		case "new":
			return NEW;
		case "return":
			return RETURN;
		case "if":
			return IF;
		case "else":
			return ELSE;
		case "while":
			return WHILE;
		case "do":
			return DO;
		case "for":
			return FOR;
		case "break":
			return BREAK;
		case "continue":
			return CONTINUE;
		case "goto":
			return GOTO;
		case "case":
			return CASE;
		case "try":
			return TRY;
		case "catch":
			return CATCH;
		case "finally":
			return FINALLY;
		case "synchronized":
			return SYNCHRONIZED;
		case "throw":
			return THROW;
		}
		return 0;
	}
	
	public static String keywordToString(int type)
	{
		switch (type)
		{
		case NULL:
			return "null";
		case TRUE:
			return "true";
		case FALSE:
			return "false";
		case THIS:
			return "this";
		case SUPER:
			return "super";
		case NEW:
			return "new";
		case RETURN:
			return "return";
		case IF:
			return "if";
		case ELSE:
			return "else";
		case WHILE:
			return "while";
		case DO:
			return "do";
		case FOR:
			return "for";
		case BREAK:
			return "break";
		case CONTINUE:
			return "continue";
		case GOTO:
			return "goto";
		case CASE:
			return "case";
		case TRY:
			return "try";
		case CATCH:
			return "catch";
		case FINALLY:
			return "finally";
		case SYNCHRONIZED:
			return "synchronized";
		case THROW:
			return "throw";
		}
		return "";
	}
}

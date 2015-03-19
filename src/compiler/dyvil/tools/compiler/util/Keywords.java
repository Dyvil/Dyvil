package dyvil.tools.compiler.util;

public interface Keywords
{
	public static int getSymbolType(String s)
	{
		switch (s)
		{
		case "_":
			return Tokens.WILDCARD;
		case ":":
			return Tokens.COLON;
		case "=":
			return Tokens.EQUALS;
		case "#":
			return Tokens.HASH;
		case "=>":
			return Tokens.ARROW_OPERATOR;
		}
		return 0;
	}
	
	public static int getKeywordType(String s)
	{
		switch (s)
		{
		case "null":
			return Tokens.NULL;
		case "true":
			return Tokens.TRUE;
		case "false":
			return Tokens.FALSE;
		case "this":
			return Tokens.THIS;
		case "super":
			return Tokens.SUPER;
		case "new":
			return Tokens.NEW;
		case "return":
			return Tokens.RETURN;
		case "if":
			return Tokens.IF;
		case "else":
			return Tokens.ELSE;
		case "while":
			return Tokens.WHILE;
		case "do":
			return Tokens.DO;
		case "for":
			return Tokens.FOR;
		case "break":
			return Tokens.BREAK;
		case "continue":
			return Tokens.CONTINUE;
		case "goto":
			return Tokens.GOTO;
		case "case":
			return Tokens.CASE;
		case "try":
			return Tokens.TRY;
		case "catch":
			return Tokens.CATCH;
		case "finally":
			return Tokens.FINALLY;
		case "synchronized":
			return Tokens.SYNCHRONIZED;
		case "throw":
			return Tokens.THROW;
		}
		return 0;
	}
	
	public static String keywordToString(int type)
	{
		// FIXME
		return "";
	}
	
	public static String symbolToString(int type)
	{
		switch (type)
		{
		case Tokens.DOT:
			return ".";
		case Tokens.COLON:
			return ":";
		case Tokens.SEMICOLON:
			return ";";
		case Tokens.COMMA:
			return ",";
		case Tokens.WILDCARD:
			return "_";
		case Tokens.EQUALS:
			return "=";
		case Tokens.HASH:
			return "#";
		case Tokens.ARROW_OPERATOR:
			return "=>";
		case Tokens.OPEN_PARENTHESIS:
			return "(";
		case Tokens.CLOSE_PARENTHESIS:
			return ")";
		case Tokens.OPEN_SQUARE_BRACKET:
			return "[";
		case Tokens.CLOSE_SQUARE_BRACKET:
			return "]";
		case Tokens.OPEN_CURLY_BRACKET:
			return "{";
		case Tokens.CLOSE_CURLY_BRACKET:
			return "}";
		}
		return null;
	}
}

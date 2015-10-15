package dyvil.tools.parsing.lexer;

public class LexerUtil
{
	
	public static boolean isOpenBracket(char c)
	{
		switch (c)
		{
		case '(':
		case '[':
		case '{':
			return true;
		}
		return false;
	}
	
	public static boolean isCloseBracket(char c)
	{
		switch (c)
		{
		case ')':
		case ']':
		case '}':
			return true;
		}
		return false;
	}
	
	public static boolean isBinDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
			return true;
		}
		return false;
	}
	
	public static boolean isOctDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
			return true;
		}
		return false;
	}
	
	public static boolean isDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			return true;
		}
		return false;
	}
	
	public static boolean isHexDigit(char c)
	{
		switch (c)
		{
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'f':
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
			return true;
		}
		return false;
	}
	
	public static boolean isIdentifierPart(char c)
	{
		if (c <= 0xA0)
		{
			return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
		}
		
		return Character.isUnicodeIdentifierPart(c);
	}
	
	public static boolean isIdentifierSymbol(char c)
	{
		if (c <= 0xA0)
		{
			switch (c)
			{
			case '.':
			case '=':
			case '>':
			case '<':
			case '+':
			case '-':
			case '*':
			case '/':
			case '!':
			case '@':
			case '#':
			case '%':
			case '^':
			case '&':
			case '~':
			case '?':
			case '|':
			case '\\':
			case ':':
				return true;
			}
			
			return false;
		}
		
		switch (Character.getType(c))
		{
		case Character.CONNECTOR_PUNCTUATION:
		case Character.DASH_PUNCTUATION:
		case Character.END_PUNCTUATION:
		case Character.START_PUNCTUATION:
		case Character.OTHER_PUNCTUATION:
		case Character.CURRENCY_SYMBOL:
		case Character.MATH_SYMBOL:
			return true;
		}
		return false;
	}
	
	public static void appendStringLiteral(String value, StringBuilder buffer)
	{
		buffer.ensureCapacity(buffer.length() + value.length() + 3);
		buffer.append('"');
		appendStringLiteralBody(value, buffer);
		buffer.append('"');
	}
	
	public static void appendStringLiteralBody(String value, StringBuilder buffer)
	{
		int len = value.length();
		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			switch (c)
			{
			case '"':
				buffer.append("\\\"");
				continue;
			case '\\':
				buffer.append("\\\\");
				continue;
			case '\n':
				buffer.append("\\n");
				continue;
			case '\t':
				buffer.append("\\t");
				continue;
			case '\r':
				buffer.append("\\r");
				continue;
			case '\b':
				buffer.append("\\b");
				continue;
			case '\f':
				buffer.append("\\f");
				continue;
			}
			buffer.append(c);
		}
	}
	
	public static void appendCharLiteral(String value, StringBuilder buffer)
	{
		buffer.ensureCapacity(buffer.length() + value.length() + 3);
		buffer.append('\'');
		appendCharLiteralBody(value, buffer);
		buffer.append('\'');
	}
	
	public static void appendCharLiteralBody(String value, StringBuilder buffer)
	{
		int len = value.length();
		for (int i = 0; i < len; i++)
		{
			char c = value.charAt(i);
			switch (c)
			{
			case '\'':
				buffer.append("\\'");
				break;
			case '\\':
				buffer.append("\\\\");
				break;
			case '\n':
				buffer.append("\\n");
				break;
			case '\t':
				buffer.append("\\t");
				break;
			case '\r':
				buffer.append("\\r");
				break;
			case '\b':
				buffer.append("\\b");
				break;
			case '\f':
				buffer.append("\\f");
				break;
			default:
				buffer.append(c);
			}
		}
	}
}

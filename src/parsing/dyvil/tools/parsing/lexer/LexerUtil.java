package dyvil.tools.parsing.lexer;

public class LexerUtil
{

	public static boolean isOpenBracket(int c)
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

	public static boolean isCloseBracket(int c)
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

	public static boolean isBinDigit(int c)
	{
		switch (c)
		{
		case '0':
		case '1':
			return true;
		}
		return false;
	}

	public static boolean isOctDigit(int c)
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

	public static boolean isDigit(int c)
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

	public static boolean isHexDigit(int c)
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

	public static boolean isIdentifierPart(int c)
	{
		if (c <= 0xA0)
		{
			return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
		}

		return Character.isUnicodeIdentifierPart(c);
	}

	public static boolean isIdentifierConnector(int c)
	{
		return c == '_' || c == '$';
	}

	public static boolean isIdentifierSymbol(int c)
	{
		if (c <= 0xA0)
		{
			switch (c)
			{
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
			if (c == '"')
			{
				buffer.append("\\\"");
				continue;
			}

			appendLiteralChar(c, buffer);
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
			if (c == '\'')
			{
				buffer.append("\\'");
				continue;
			}

			appendLiteralChar(c, buffer);
		}
	}

	private static void appendLiteralChar(char c, StringBuilder buffer)
	{
		switch (c)
		{
		case '\\':
			buffer.append("\\\\");
			return;
		case '\n':
			buffer.append("\\n");
			return;
		case '\t':
			buffer.append("\\t");
			return;
		case '\r':
			buffer.append("\\r");
			return;
		case '\b':
			buffer.append("\\b");
			return;
		case '\f':
			buffer.append("\\f");
			return;
		case '\u000B':
			buffer.append("\\v");
			return;
		case '\u0007':
			buffer.append("\\a");
			return;
		case '\u001B':
			buffer.append("\\e");
			return;
		case '\0':
			buffer.append("\\0");
			return;
		}

		if (c < 256 && c >= 32)
		{
			buffer.append(c);
			return;
		}
		buffer.append("\\u{").append(Integer.toHexString(c)).append('}');
	}
}

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

}

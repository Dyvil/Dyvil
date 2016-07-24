package dyvil.tools.parsing.name;

public class Qualifier
{
	public static final Qualifier INSTANCE = new Qualifier();

	public static final String AMPERSAND     = "amp";
	public static final String ASTERISK      = "times";
	public static final String AT            = "at";
	public static final String BACKSLASH     = "bslash";
	public static final String BAR           = "bar";
	public static final String CARET         = "up";
	public static final String COLON         = "colon";
	public static final String DOT           = "dot";
	public static final String EQ            = "eq";
	public static final String EXCLAMATION   = "bang";
	public static final String GREATER       = "gt";
	public static final String HASH          = "hash";
	public static final String LESS          = "lt";
	public static final String MINUS         = "minus";
	public static final String PERCENT       = "percent";
	public static final String PLUS          = "plus";
	public static final String QUESTION_MARK = "qmark";
	public static final String SLASH         = "div";
	public static final String TILDE         = "tilde";

	public char replaceString(String str)
	{
		// @formatter:off
		switch (str)
		{
		case AMPERSAND:     return '&';
		case ASTERISK:      return '*';
		case AT:            return '@';
		case BACKSLASH:     return '\\';
		case BAR:           return '|';
		case CARET:         return '^';
		case COLON:         return ':';
		case DOT:           return '.';
		case EQ:            return '=';
		case EXCLAMATION:   return '!';
		case GREATER:       return '>';
		case HASH:          return '#';
		case LESS:          return '<';
		case MINUS:         return '-';
		case PERCENT:       return '%';
		case PLUS:          return '+';
		case QUESTION_MARK: return '?';
		case SLASH:         return '/';
		case TILDE:         return '~';
		}
		// @formatter:on
		return 0;
	}

	public String replaceChar(char c)
	{
		switch (c)
		{
		case '&':
			return AMPERSAND;
		case '*':
			return ASTERISK;
		case '@':
			return AT;
		case '\\':
			return BACKSLASH;
		case '|':
			return BAR;
		case '^':
			return CARET;
		case ':':
			return COLON;
		case '.':
			return DOT;
		case '=':
			return EQ;
		case '!':
			return EXCLAMATION;
		case '>':
			return GREATER;
		case '#':
			return HASH;
		case '<':
			return LESS;
		case '-':
			return MINUS;
		case '%':
			return PERCENT;
		case '+':
			return PLUS;
		case '?':
			return QUESTION_MARK;
		case '/':
			return SLASH;
		case '~':
			return TILDE;
		}
		return null;
	}

	public static String qualify(String s)
	{
		return qualify(s, INSTANCE);
	}

	public static String qualify(String s, Qualifier qualifier)
	{
		final int length = s.length();
		final StringBuilder builder = new StringBuilder(length);

		for (int i = 0; i < length; i++)
		{
			final char c = s.charAt(i);
			final String replacement = qualifier.replaceChar(c);

			if (replacement != null)
			{
				builder.append('$');
				builder.append(replacement);
			}
			else
			{
				builder.append(c);
			}
		}

		return builder.toString();
	}

	public static String unqualify(String s)
	{
		return unqualify(s, INSTANCE);
	}

	public static String unqualify(String string, Qualifier qualifier)
	{
		int index = string.indexOf('$');
		if (index < 0)
		{
			// no $ in string - don't apply any replacements
			return string;
		}

		final int len = string.length();
		final StringBuilder builder = new StringBuilder(len);

		// append all characters before the first $
		builder.append(string, 0, index);

		for (; index < len; index++)
		{
			final char c = string.charAt(index);
			if (c != '$')
			{
				builder.append(c);
				continue;
			}

			final int startIndex = index + 1;
			final int endIndex = symbolEndIndex(string, startIndex, len);

			if (startIndex == endIndex)
			{
				// $$ or $_
				builder.append('$');
				continue;
			}

			index = endIndex - 1;
			final String toReplace = string.substring(startIndex, endIndex);
			final char replacement = qualifier.replaceString(toReplace);

			if (replacement > 0)
			{
				builder.append(replacement);
			}
			else
			{
				builder.append('$').append(toReplace);
			}
		}
		return builder.toString();
	}

	private static int symbolEndIndex(String string, int start, int end)
	{
		for (; start < end; start++)
		{
			switch (string.charAt(start))
			{
			case '$':
			case '_':
				return start;
			}
		}
		return end;
	}
}

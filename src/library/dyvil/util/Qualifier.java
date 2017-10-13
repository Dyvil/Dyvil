package dyvil.util;

import dyvil.annotation.internal.NonNull;

public class Qualifier
{
	public static final @NonNull Qualifier INSTANCE = new Qualifier();

	public static final @NonNull String AMPERSAND     = "amp";
	public static final @NonNull String ASTERISK      = "times";
	public static final @NonNull String AT            = "at";
	public static final @NonNull String BACKSLASH     = "bslash";
	public static final @NonNull String BAR           = "bar";
	public static final @NonNull String CARET         = "up";
	public static final @NonNull String COLON         = "colon";
	public static final @NonNull String DOT           = "dot";
	public static final @NonNull String EQ            = "eq";
	public static final @NonNull String EXCLAMATION   = "bang";
	public static final @NonNull String GREATER       = "gt";
	public static final @NonNull String HASH          = "hash";
	public static final @NonNull String LESS          = "lt";
	public static final @NonNull String MINUS         = "minus";
	public static final @NonNull String PERCENT       = "percent";
	public static final @NonNull String PLUS          = "plus";
	public static final @NonNull String QUESTION_MARK = "qmark";
	public static final @NonNull String SLASH         = "div";
	public static final @NonNull String TILDE         = "tilde";

	public int replaceString(@NonNull String str)
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

	public @NonNull String replaceCodePoint(int c)
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

	public static @NonNull String qualify(@NonNull String string)
	{
		return qualify(string, INSTANCE);
	}

	public static @NonNull String qualify(@NonNull String string, @NonNull Qualifier qualifier)
	{
		final int length = string.length();
		final StringBuilder builder = new StringBuilder(length);

		for (int i = 0; i < length; )
		{
			final int codePoint = string.codePointAt(i);
			final String replacement = qualifier.replaceCodePoint(codePoint);

			if (replacement != null)
			{
				builder.append('$');
				builder.append(replacement);
			}
			else
			{
				builder.appendCodePoint(codePoint);
			}

			i += Character.charCount(codePoint);
		}

		return builder.toString();
	}

	public static @NonNull String unqualify(@NonNull String string)
	{
		return unqualify(string, INSTANCE);
	}

	public static @NonNull String unqualify(@NonNull String string, @NonNull Qualifier qualifier)
	{
		int appendStart = string.indexOf('$');
		if (appendStart < 0)
		{
			// no $ in string - don't apply any replacements
			return string;
		}

		final int len = string.length();
		final StringBuilder builder = new StringBuilder(len);

		// append all characters before the first $
		builder.append(string, 0, appendStart);
		int searchIndex = appendStart;

		while (searchIndex < len)
		{
			final int cashIndex = string.indexOf('$', searchIndex);
			final int startIndex = cashIndex + 1;
			final int endIndex = symbolEndIndex(string, startIndex, len);
			searchIndex = endIndex;

			if (startIndex == endIndex)
			{
				// $$ or $_
				continue;
			}

			final String key = string.substring(startIndex, endIndex);
			final int replacement = qualifier.replaceString(key);

			if (replacement > 0)
			{
				builder.append(string, appendStart, cashIndex);
				builder.appendCodePoint(replacement);
				appendStart = endIndex;
			}
		}

		builder.append(string, appendStart, len);
		return builder.toString();
	}

	private static int symbolEndIndex(@NonNull String string, int start, int end)
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

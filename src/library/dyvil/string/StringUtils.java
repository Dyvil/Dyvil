package dyvil.string;

import dyvil.annotation.Utility;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.collection.List;
import dyvil.collection.immutable.EmptyList;
import dyvil.collection.mutable.ArrayList;
import dyvil.math.MathUtils;
import dyvil.reflect.Modifiers;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@linkplain Utility utility interface} <b>StringUtils</b> can be used for several String-related functions such
 * as splitting a string into a list of words, converting it to an identifier or acronym, converting to Title Case or
 * camelCase, counting the number of times a character appears a the string, getting the index of a Regular Expression
 * as well as several useful utility functions.
 *
 * @author Clashsoft
 * @version 1.0
 */
@Utility(String.class)
public final class StringUtils
{
	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	private StringUtils()
	{
		// no instances
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String repeated(String string, int count)
	{
		switch (count)
		{
		case 0:
			return "";
		case 1:
			return string;
		case 2:
			if (string == null)
			{
				return "nullnull";
			}
			return string.concat(string);
		}

		if (string == null)
		{
			string = "null";
		}

		StringBuilder builder = new StringBuilder(string.length() * count);
		for (int i = 0; i < count; i++)
		{
			builder.append(string);
		}

		return builder.toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String[] split(String string, char character)
	{
		if (string == null || string.isEmpty())
		{
			return EMPTY_STRING_ARRAY;
		}

		int len = string.length();
		ArrayList<String> list = new ArrayList<>(len >> 4);

		int startIndex = 0;
		for (int i = 0; i < len; i++)
		{
			if (string.charAt(i) == character)
			{
				if (i - startIndex > 0)
				{
					list.add(string.substring(startIndex, i));
				}
				startIndex = i + 1;
			}
		}
		if (len - startIndex > 0)
		{
			list.add(string.substring(startIndex, len));
		}

		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String[] words(String string)
	{
		if (string == null || string.isEmpty())
		{
			return EMPTY_STRING_ARRAY;
		}
		return string.split("\\s");
	}

	/**
	 * Returns a list of words contained in the given {@code string}. A 'word' is described as a sequence of letter
	 * characters, which are themselves described in terms of {@link CharUtils#isLetter(char)}. Every other non-etter
	 * character is simply ommitted from the list of words.
	 *
	 * @param string
	 * 	the string to split
	 *
	 * @return a list of words in the given string
	 */

	@DyvilModifiers(Modifiers.INFIX)
	public static List<String> wordList(String string)
	{
		if (string == null || string.isEmpty())
		{
			return EmptyList.apply();
		}
		return List.apply(words(string));
	}

	/**
	 * Splits the given {@code string} into an array of lines separated by newline ({@code \n}) characters using it's
	 * {@link String#split(String) split(String)} method
	 *
	 * @param string
	 * 	the string to split
	 *
	 * @return an array of lines
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static String[] lines(String string)
	{
		if (string == null || string.isEmpty())
		{
			return EMPTY_STRING_ARRAY;
		}
		return split(string, '\n');
	}

	/**
	 * Splits the given {@code string} into a {@link List} of lines separated by newline ({@code \n}) characters using
	 * it's {@link String#split(String) split(String)} method
	 *
	 * @param string
	 * 	the string to split
	 *
	 * @return a List of lines
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static List<String> lineList(String string)
	{
		if (string == null || string.isEmpty())
		{
			return EmptyList.apply();
		}

		return List.apply(lines(string));
	}

	/**
	 * Returns the Levenshtein distance between the given {@link String Strings} {@code s1} and {@code s2}.
	 *
	 * @param s1
	 * 	the first string
	 * @param s2
	 * 	the second string
	 *
	 * @return the Levenshtein distance between the two strings
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static int distanceTo(String s1, String s2)
	{
		if (s1.equals(s2))
		{
			return 0;
		}
		int len1 = s1.length();
		if (len1 == 0)
		{
			return s2.length();
		}
		int len2 = s2.length();
		if (len2 == 0)
		{
			return len1;
		}

		int i;
		int j;
		int alen = len2 + 1;
		int[] a1 = new int[alen];
		int[] a2 = new int[alen];

		for (i = 0; i < alen; i++)
		{
			a1[i] = i;
		}
		for (i = 0; i < len1; i++)
		{
			a2[0] = i + 1;
			for (j = 0; j < len2; j++)
			{
				a2[j + 1] = s1.charAt(i) == s2.charAt(j) ?
					            MathUtils.min(a2[j] + 1, a1[j + 1] + 1, a1[j]) :
					            MathUtils.min(a2[j] + 1, a1[j + 1] + 1, a1[j] + 1);
			}
			System.arraycopy(a2, 0, a1, 0, alen);
		}
		return a2[len2];
	}

	/**
	 * Converts the given {@code string} to a valid lower-case identifier. This is done by replacing all whitespace
	 * characters in the string with underscores ({@code _}) and converting all other characters to lower-case.
	 *
	 * @param string
	 * 	the string to convert
	 *
	 * @return the string converted to a valid identifier
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static String toIdentifier(String string)
	{
		int len = string.length();
		StringBuilder result = new StringBuilder(len);

		for (int i = 0; i < len; i++)
		{
			char c = string.charAt(i);

			if (CharUtils.isWhitespace(c))
			{
				c = '_';
			}
			else
			{
				c = CharUtils.toLowerCase(c);
			}

			result.append(c);
		}

		return result.toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toTitleCase(String s)
	{
		if (s == null)
		{
			return null;
		}

		int len = s.length();
		if (len <= 0)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder(len);

		boolean seperator = true;
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			if (CharUtils.isWhitespace(c))
			{
				seperator = true;
				builder.append(c);
				continue;
			}

			if (seperator)
			{
				seperator = false;
				builder.append(CharUtils.toUpperCase(c));
				continue;
			}
			builder.append(CharUtils.toLowerCase(c));
		}

		return builder.toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toLowerCamelCase(String s)
	{
		if (s == null)
		{
			return null;
		}

		int len = s.length();
		if (len <= 0)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder(len);

		boolean seperator = true;
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			if (CharUtils.isWhitespace(c))
			{
				seperator = true;
				builder.append(c);
				continue;
			}

			if (seperator)
			{
				seperator = false;
				builder.append(CharUtils.toLowerCase(c));
				continue;
			}
			builder.append(c);
		}

		return builder.toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toUpperCamelCase(String s)
	{
		if (s == null)
		{
			return null;
		}

		int len = s.length();
		if (len <= 0)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder(len);

		boolean seperator = true;
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			if (CharUtils.isWhitespace(c))
			{
				seperator = true;
				builder.append(c);
				continue;
			}

			if (seperator)
			{
				seperator = false;
				builder.append(CharUtils.toUpperCase(c));
				continue;
			}
			builder.append(c);
		}

		return builder.toString();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static String toInvertedCase(String s)
	{
		if (s == null)
		{
			return null;
		}

		int len = s.length();
		if (len <= 0)
		{
			return "";
		}

		StringBuilder builder = new StringBuilder(len);

		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			builder.append(CharUtils.invertCase(c));
		}

		return builder.toString();
	}

	/**
	 * Counts the number of times the given {@code char character} appears in the given {@link String} {@code text}.
	 *
	 * @param string
	 * 	the input string
	 * @param character
	 * 	the character to search for
	 *
	 * @return the number of times the character appears in the string
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static int count(String string, char character)
	{
		int count = 0;
		int len = string.length();
		for (int i = 0; i < len; i++)
		{
			if (string.charAt(i) == character)
			{
				count++;
			}
		}
		return count;
	}

	/**
	 * Checks if the given {@link String} {@code text} contains the given {@code char character}.
	 *
	 * @param string
	 * 	the string
	 * @param character
	 * 	the character
	 *
	 * @return true, if the string contains the character
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean contains(String string, char character)
	{
		return string.indexOf(character) != -1;
	}

	/**
	 * Checks if the given {@link String} {@code text} contains the regular expression given by the {@link String}
	 * {@code regex}.
	 *
	 * @param string
	 * 	the string
	 * @param regex
	 * 	the regular expression
	 *
	 * @return true, if the string contains the regular expression
	 *
	 * @see Pattern
	 * @see Matcher#find()
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean containsRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).find();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOfRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).start();
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int indexOfBounded(String string, String pattern, int startIndex, int endIndex)
	{
		final int index = string.indexOf(pattern, startIndex);
		return index + pattern.length() <= endIndex ? index : -1;
	}

	public static <T> void prettyPrint(T value, Class<T> type, StringBuilder builder, boolean fieldNames)
	{
		Field[] fields = type.getFields();
		builder.append(type.getName());

		builder.append('(');
		int count = 0;
		for (Field field : fields)
		{
			if ((field.getModifiers() & Modifiers.STATIC) != 0)
			{
				continue;
			}

			if (count++ > 0)
			{
				builder.append(", ");
			}

			if (fieldNames)
			{
				builder.append(field.getName()).append(": ");
			}

			try
			{
				field.setAccessible(true);
				builder.append(field.get(value));
			}
			catch (IllegalArgumentException | IllegalAccessException ex)
			{
				ex.printStackTrace();
				builder.append("<error>");
			}
		}

		builder.append(')');
	}
}

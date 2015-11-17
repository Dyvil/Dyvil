package dyvil.string;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dyvil.annotation.Utility;
import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.math.MathUtils;
import dyvil.random.RandomUtils;
import dyvil.reflect.Modifiers;

/**
 * The {@linkplain Utility utility interface} <b>StringUtils</b> can be used for
 * several String-related functions such as splitting a string into a list of
 * words, converting it to an identifier or acronym, converting to Title Case or
 * camelCase, counting the number of times a character appears a the string,
 * getting the index of a Regular Expression as well as several useful utility
 * functions.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Utility(String.class)
public interface StringUtils
{
	String[] EMPTY_STRING_ARRAY = new String[0];
	
	static @infix @inline String $times(int count, String string)
	{
		return $times(string, count);
	}
	
	static @infix String $times(String string, int count)
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
	
	/**
	 * Formats the given {@link String} {@code format} with the given
	 * {@code Object[] args} using {@link String#format(String, Object...)}.
	 * 
	 * @param format
	 *            the format String
	 * @param args
	 *            the format arguments
	 * @return the formatted String
	 */
	static @infix @inline String format(String format, Object... args)
	{
		return String.format(format, args);
	}
	
	static @infix String[] words(String string)
	{
		List<String> words = wordList(string);
		String[] array = new String[words.size()];
		words.toArray(array);
		return array;
	}
	
	/**
	 * Returns a list of words contained in the given {@code string}. A 'word'
	 * is described as a sequence of letter characters, which are themselves
	 * described in terms of {@link CharUtils#isLetter(char)}. Every other
	 * non-etter character is simply ommitted from the list of words.
	 * 
	 * @param string
	 *            the string to split
	 * @return a list of words in the given string
	 */
	static @infix List<String> wordList(String string)
	{
		List<String> words = new ArrayList();
		StringBuilder buffer = new StringBuilder(10);
		
		for (int i = 0; i < string.length(); i++)
		{
			char c = string.charAt(i);
			if (!CharUtils.isLetter(c))
			{
				words.add(buffer.toString());
				buffer.delete(0, buffer.length());
				continue;
			}
			buffer.append(c);
		}
		
		if (buffer.length() > 0)
		{
			words.add(buffer.toString());
		}
		
		return words;
	}
	
	/**
	 * Trims the given {@code string} to lines of the maximum length
	 * {@code maxLength}. This is done by {@link String#split(String) splitting}
	 * the {@code string} into a list of tokens separated by whitespace
	 * characters, and adding the words separated by a single whitespace to a
	 * line buffer. As soon as the line buffer reaches the {@code maxLength},
	 * the algorithm adds the line to a list collecting the lines and proceeds
	 * with a new, empty line. This is done until all words have been processed,
	 * at which point the generated list of lines will be returned.
	 * 
	 * @param string
	 * @param maxLength
	 * @return
	 */
	static @infix List<String> trimLineLength(String string, int maxLength)
	{
		String[] words = string.split("\\s");
		StringBuilder buffer = new StringBuilder(10);
		List<String> lines = new ArrayList();
		
		for (String word : words)
		{
			if (buffer.length() + word.length() >= maxLength)
			{
				lines.add(buffer.toString());
				buffer.delete(0, buffer.length());
			}
			buffer.append(word).append(' ');
		}
		if (buffer.length() > 0)
		{
			lines.add(buffer.toString());
		}
		
		return lines;
	}
	
	/**
	 * Splits the given {@code string} into an array of lines separated by
	 * newline ({@code \n}) characters using it's {@link String#split(String)
	 * split(String)} method
	 * 
	 * @param string
	 *            the string to split
	 * @return an array of lines
	 */
	static @infix String[] lines(String string)
	{
		if (string == null || string.isEmpty())
		{
			return EMPTY_STRING_ARRAY;
		}
		return string.split("\n");
	}
	
	/**
	 * Splits the given {@code string} into a {@link List} of lines separated by
	 * newline ({@code \n}) characters using it's {@link String#split(String)
	 * split(String)} method
	 * 
	 * @param string
	 *            the string to split
	 * @return a List of lines
	 */
	static List<String> lineList(String string)
	{
		if (string == null)
		{
			return new ArrayList();
		}
		return new ArrayList(string.split("\n"), true);
	}
	
	/**
	 * Returns the Levenshtein distance between the given {@link String Strings}
	 * {@code s1} and {@code s2}.
	 * 
	 * @param s1
	 *            the first string
	 * @param s2
	 *            the second string
	 * @return the Levenshtein distance between the two strings
	 */
	static @infix int distanceTo(String s1, String s2)
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
				a2[j + 1] = s1.charAt(i) == s2.charAt(j) ? MathUtils.min(a2[j] + 1, a1[j + 1] + 1, a1[j]) : MathUtils.min(a2[j] + 1, a1[j + 1] + 1, a1[j] + 1);
			}
			System.arraycopy(a2, 0, a1, 0, alen);
		}
		return a2[len2];
	}
	
	/**
	 * Converts the given {@code string} to a valid lower-case identifier. This
	 * is done by replacing all whitespace characters in the string with
	 * underscores ({@code _}) and converting all other characters to
	 * lower-case.
	 * 
	 * @param string
	 *            the string to convert
	 * @return the string converted to a valid identifier
	 */
	static @infix String toIdentifier(String string)
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
	
	/**
	 * Converts the given {@code string} to an acronym by removing all
	 * characters but those at the beginning of a new word.
	 * <p>
	 * Example:<br>
	 * {@code getAcronym("Hello World")} returns "HW";
	 * {@code getAcronym("Half-Life 3")} returns "HL3"
	 * 
	 * @param string
	 *            the string
	 * @return the acronym of the string
	 */
	static @infix String toAcronym(String string)
	{
		if (string == null)
		{
			return null;
		}
		
		int len = string.length();
		if (len <= 0)
		{
			return "";
		}
		
		StringBuilder builder = new StringBuilder(len >> 2);
		
		boolean seperator = true;
		for (int i = 0; i < len; i++)
		{
			char c = string.charAt(i);
			if (!CharUtils.isLetter(c))
			{
				if (CharUtils.isDigit(c))
				{
					builder.append(c);
				}
				seperator = true;
				continue;
			}
			
			if (seperator)
			{
				builder.append(c);
				seperator = false;
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * Removes the vowels of the given {@code string} in such a way that it is
	 * still readable and identifiable. This is done by only removing vowels
	 * that have a consonant to the left <i>and</i> to the right.
	 * <p>
	 * Vowels are defined in terms of {@link CharUtils#isVowel(char)}<br>
	 * Consonants are defined in terms of {@link CharUtils#isConsonant(char)}
	 * 
	 * @param string
	 *            the string to remove the vowels from
	 * @return a readable acronym-like version of the string with most vowels
	 *         removed
	 */
	static @infix String removeVowels(String string)
	{
		if (string == null)
		{
			return null;
		}
		
		int len = string.length();
		if (len <= 0)
		{
			return "";
		}
		
		StringBuilder builder = new StringBuilder(len);
		
		char prev = 0;
		char curr = 0;
		char next = string.charAt(0);
		for (int i = 1; i < len; i++)
		{
			prev = curr;
			curr = next;
			next = string.charAt(i);
			
			if (CharUtils.isVowel(curr) && CharUtils.isConsonant(prev) && CharUtils.isConsonant(next))
			{
				continue;
			}
			builder.append(curr);
		}
		return builder.append(next).toString();
	}
	
	static @infix String toTitleCase(String s)
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
	
	static @infix String toLowerCamelCase(String s)
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
	
	static @infix String toUpperCamelCase(String s)
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
	
	static @infix String toInvertedCase(String s)
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
	 * Counts the number of times the given {@code char c} appears in the given
	 * {@link String} {@code text}.
	 * 
	 * @param string
	 *            the input string
	 * @param c
	 *            the character to search for
	 * @return the number of times the character appears in the string
	 */
	static @infix int count(String string, char c)
	{
		int count = 0;
		int len = string.length();
		for (int i = 0; i < len; i++)
		{
			if (string.charAt(i) == c)
			{
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Checks if the given {@link String} {@code text} contains the given
	 * {@code char c}.
	 * 
	 * @param string
	 *            the string
	 * @param c
	 *            the character
	 * @return true, if the string contains the character
	 */
	static @infix boolean contains(String string, char c)
	{
		return string.indexOf(c) != -1;
	}
	
	/**
	 * Checks if the given {@link String} {@code text} contains the regular
	 * expression given by the {@link String} {@code regex}.
	 * 
	 * @see Pattern
	 * @see Matcher#find()
	 * @param string
	 *            the string
	 * @param regex
	 *            the regular expression
	 * @return true, if the string contains the regular expression
	 */
	static boolean containsRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).find();
	}
	
	static boolean containsAny(String string, String regex)
	{
		return indexOfAny(string, regex) != -1;
	}
	
	static int indexOfRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).start();
	}
	
	static int indexOfAny(String string, String regex)
	{
		for (int i = 0; i < regex.length(); i++)
		{
			int index = string.indexOf(regex.charAt(i));
			if (index != -1)
			{
				return index;
			}
		}
		return -1;
	}
	
	static int indexOfRange(String string, String regex, int min, int max)
	{
		int index = string.indexOf(regex, min);
		return index < max ? index : -1;
	}
	
	/**
	 * Returns a new random name.
	 * 
	 * @param random
	 *            the random
	 * @param minLength
	 *            the min length
	 * @param maxLength
	 *            the max length
	 * @return the next random name
	 */
	static @infix String nextNoun(java.util.Random random, int minLength, int maxLength)
	{
		int len = RandomUtils.nextInt(random, minLength, maxLength);
		StringBuilder buf = new StringBuilder(len);
		
		char prev = CharUtils.nextUppercaseLetter(random);
		buf.append(prev);
		for (int i = 1; i < len; i++)
		{
			char c;
			
			if (CharUtils.isVowel(prev))
			{
				// Always add a consonant after a vowel
				c = CharUtils.nextConsonant(random);
			}
			else
			{
				int rnd = random.nextInt(6);
				if (rnd < 4)
				{
					// Add a consonant
					c = CharUtils.nextConsonant(random);
					int i1 = 0;
					while (!CharUtils.isCombinable(prev, c) && i1++ <= 21)
					{
						c = CharUtils.nextConsonant(random);
					}
					
					if (i1 > 21)
					{
						c = CharUtils.nextVowel(random);
					}
				}
				else
				{
					// Add a vowel
					c = CharUtils.nextVowel(random);
				}
			}
			
			prev = c;
			buf.append(c);
		}
		
		return buf.toString();
	}
	
	static <T> void prettyPrint(T value, Class<T> type, StringBuilder builder, boolean fieldNames)
	{
		Field[] fields = type.getFields();
		builder.append(type.getName());
		
		builder.append('(');
		int count = 0;
		for (Field f : fields)
		{
			if ((f.getModifiers() & Modifiers.STATIC) != 0)
			{
				continue;
			}
			
			if (count++ > 0)
			{
				builder.append(", ");
			}
			
			if (fieldNames)
			{
				builder.append(f.getName()).append(": ");
			}
			
			try
			{
				f.setAccessible(true);
				builder.append(f.get(value));
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

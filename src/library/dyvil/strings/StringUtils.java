package dyvil.strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dyvil.annotation.Utility;
import dyvil.annotation.infix;
import dyvil.annotation.inline;
import dyvil.math.MathUtils;
import dyvil.random.Random;

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
	public static @infix @inline String format(String format, Object... args)
	{
		return String.format(format, args);
	}
	
	public static List<String> getWords(String s, boolean spacesOnly)
	{
		List<String> words = new ArrayList();
		StringBuilder temp = new StringBuilder(10);
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			temp.append(c);
			if (CharUtils.isWhitespace(c) || !spacesOnly && !CharUtils.isLetter(c))
			{
				words.add(temp.toString());
				temp.delete(0, temp.length());
				continue;
			}
		}
		
		if (temp.length() > 0)
		{
			words.add(temp.toString());
		}
		
		return words;
	}
	
	public static List<String> cutString(String s, int maxLength)
	{
		List<String> words = getWords(s, false);
		StringBuilder temp = new StringBuilder(10);
		List<String> lines = new ArrayList();
		
		for (String word : words)
		{
			if (temp.length() + word.length() >= maxLength)
			{
				lines.add(temp.toString());
				temp.delete(0, temp.length());
			}
			temp.append(word);
		}
		if (temp.length() > 0)
		{
			lines.add(temp.toString());
		}
		
		return lines;
	}
	
	public static String[] lines(String s)
	{
		if (s == null)
		{
			return dyvil.arrays.ArrayUtils.EMPTY_STRING_ARRAY;
		}
		return s.split("\n");
	}
	
	public static List<String> lineList(String s)
	{
		if (s == null)
		{
			return Collections.EMPTY_LIST;
		}
		return Arrays.asList(s.split("\n"));
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
	public static int distance(String s1, String s2)
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
	
	public static @infix String toIdentifier(String s)
	{
		int len = s.length();
		StringBuilder result = new StringBuilder(len);
		
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
			
			if (Character.isWhitespace(c))
			{
				c = '_';
			}
			else
			{
				c = Character.toLowerCase(c);
			}
			
			result.append(c);
		}
		
		return result.toString();
	}
	
	/**
	 * Returns the acronym of the given {@link String} {@code s} by removing all
	 * characters but those at the beginning of a new word.
	 * <p>
	 * Example:<br>
	 * {@code getAcronym("Hello World")} returns "HW";
	 * {@code getAcronym("Half-Life 3")} returns "HL3"
	 * 
	 * @param string
	 *            the string
	 * @return the initials
	 */
	public static @infix String toAcronym(String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		
		int len = s.length();
		StringBuilder builder = new StringBuilder(len >> 2);
		
		boolean seperator = true;
		for (int i = 0; i < len; i++)
		{
			char c = s.charAt(i);
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
	
	public static @infix String removeVowels(String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		
		int len = s.length();
		StringBuilder builder = new StringBuilder(len);
		
		char prev = 0;
		char curr = 0;
		char next = s.charAt(0);
		for (int i = 1; i < len; i++)
		{
			prev = curr;
			curr = next;
			next = s.charAt(i);
			
			if (CharUtils.isVowel(curr) && CharUtils.isConsonant(prev) && CharUtils.isConsonant(next))
			{
				continue;
			}
			builder.append(curr);
		}
		return builder.toString();
	}
	
	public static @infix String toTitleCase(String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		
		int len = s.length();
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
	
	public static @infix String toLowerCamelCase(String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		
		int len = s.length();
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
	
	public static @infix String toUpperCamelCase(String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		
		int len = s.length();
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
	
	public static @infix String toInvertedCase(String s)
	{
		if (s == null || s.isEmpty())
		{
			return "";
		}
		
		int len = s.length();
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
	public static @infix int count(String string, char c)
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
	public static @infix boolean contains(String string, char c)
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
	public static boolean containsRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).find();
	}
	
	public static boolean containsAny(String string, String regex)
	{
		return indexOfAny(string, regex) != -1;
	}
	
	public static int indexOfRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).start();
	}
	
	public static int indexOfAny(String string, String regex)
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
	
	public static int indexOfRange(String string, String regex, int min, int max)
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
	public static @infix String nextNoun(java.util.Random random, int minLength, int maxLength)
	{
		int len = Random.nextInt(random, minLength, maxLength);
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
}

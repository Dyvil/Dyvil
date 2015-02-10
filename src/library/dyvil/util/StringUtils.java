package dyvil.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dyvil.lang.annotation.infix;
import dyvil.random.Random;

public interface StringUtils
{
	public static final String[]	EMPTY_STRING_ARRAY	= new String[0];
	
	public static @infix String format(String format, Object... args)
	{
		return String.format(format, args);
	}
	
	public static String identifier(String string)
	{
		int len = string.length();
		StringBuilder result = new StringBuilder(len);
		
		for (int i = 0; i < len; i++)
		{
			char c = string.charAt(i);
			
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
	
	public static List<String> getWords(String string, boolean spacesOnly)
	{
		List<String> words = new ArrayList();
		StringBuilder temp = new StringBuilder(10);
		char l = 0;
		
		for (int i = 0; i < string.length(); i++)
		{
			char c = string.charAt(i);
			temp.append(c);
			if (CharUtils.isWhitespace(c) || !spacesOnly && !CharUtils.isLetter(c))
			{
				words.add(temp.toString());
				temp.delete(0, temp.length());
				continue;
			}
			
			l = c;
		}
		
		if (temp.length() > 0)
		{
			words.add(temp.toString());
		}
		
		return words;
	}
	
	public static List<String> cutString(String string, int maxLength)
	{
		List<String> words = getWords(string, false);
		int size = words.size();
		StringBuilder temp = new StringBuilder(10);
		List<String> lines = new ArrayList();
		
		int i = 0;
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
	
	public static String[] lines(String string)
	{
		if (string == null)
		{
			return EMPTY_STRING_ARRAY;
		}
		return string.split("\n");
	}
	
	public static List<String> lineList(String string)
	{
		if (string == null)
		{
			return Collections.EMPTY_LIST;
		}
		return Arrays.asList(string.split("\n"));
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
	public static String getAcronym(String s)
	{
		return s; // FIXME
	}
	
/**
	 * Returns a shortened acronym version of the given {@link String} {@code s} by removing the vowels.
	 * <p>
	 * Example:<br>
	 * {@code getAcronym2("Hello World") returns "Hllo Wrld"
	 * {@code getAcronym2("Clashsoft") returns "Clshsft"
	 * @param s
	 * @return
	 */
	public static String removeVowels(String s)
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
	
	public static String toLowerCamelCase(String s)
	{
		return s; // FIXME
	}
	
	public static String toUpperCamelCase(String s)
	{
		return s; // FIXME
	}
	
	public static String toInvertedCase(String s)
	{
		return s; // FIXME
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
}

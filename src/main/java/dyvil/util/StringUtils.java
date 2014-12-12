package dyvil.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

import dyvil.lang.array.Arrays;

/**
 * The Class CSString.
 * <p>
 * This class adds several string tools.
 * 
 * @see String
 * @see StringBuilder
 * @see StringBuffer
 * @author Clashsoft
 */
public class StringUtils
{
	private static final String[]	ROMANCODE				= { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
	private static final int[]		BINEQUAL				= { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
	
	public static final String[]	fastRomanCache			= { "0", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX" };
	
	/** The Alphabet. */
	public static final String		ALPHABET				= "abcdefghijklmnopqrstuvwxyz";
	
	/** Vowels. */
	public static final String		VOWELS					= "aeiou";
	
	/** Consonants. */
	public static final String		CONSONANTS				= "bcdfghjklmnpqrstvwxyz";
	
	/** Possible consonant combinations in the english language. */
	public static final String[]	CONSONANTCOMBINATIONS	= { "bl", "br", "cl", "cr", "dr", "fl", "fr", "gl", "gr", "pl", "pr", "sh", "sk", "sl", "sm", "sn", "sp", "st", "sw", "tr", "tw", "th" };
	
	/**
	 * Case constants.
	 * 
	 * @see StringUtils#caseString(String, int)
	 */
	public static final int			LOWERCASE				= 0, UPPERCASE = 1, LOWER_CAMELCASE = 2, UPPER_CAMELCASE = 3, INVERTED_CASE = 4, INVERTED_LOWER_CAMELCASE = 5, INVERTED_UPPER_CAMELCASE = 6;
	
	public static String format(String format, Object... args)
	{
		return String.format(format, args);
	}
	
	public static boolean isNumber(String string)
	{
		try
		{
			Double.parseDouble(string);
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
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
	
	/**
	 * Converts a number to a roman number.
	 * 
	 * @param number
	 *            the number
	 * @return the roman number
	 */
	public static String toRomanString(int number)
	{
		if (number < 0)
		{
			return "-" + toRomanString(-number);
		}
		else if (number < fastRomanCache.length)
		{
			return fastRomanCache[number];
		}
		else if (number >= 4000)
		{
			throw new NumberFormatException("Exception while converting to Roman: Value outside roman numeral range.");
		}
		
		StringBuilder roman = new StringBuilder();
		
		for (int i = 0; i < ROMANCODE.length; i++)
		{
			while (number >= BINEQUAL[i])
			{
				number -= BINEQUAL[i];
				roman.append(ROMANCODE[i]);
			}
		}
		return roman.toString();
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
			if (isSpace(c) || !spacesOnly && !isLetter(c))
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
	
	/**
	 * Wraps a string to make multiple lines with a maximum length. It doesn't
	 * cut words
	 * 
	 * @param string
	 *            the string
	 * @param maxLength
	 *            the max line length
	 * @return the string
	 */
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
	
	/**
	 * Makes a line array for rendering.
	 * 
	 * @param string
	 *            the string
	 * @return the string[]
	 */
	public static String[] lineArray(String string)
	{
		if (string == null)
		{
			return new String[0];
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
	 * Returns the acronym of a string.
	 * <p>
	 * Example:<br>
	 * {@code getAcronym("Hello World")} returns "HW";
	 * {@code getAcronym("Half-Life 3")} returns "HL3"
	 * 
	 * @param string
	 *            the string
	 * @return the initials
	 */
	public static String getAcronym(String string)
	{
		StringBuilder builder = new StringBuilder(string.length() >> 2);
		String[] strings = string.split("\\p{Punct}");
		for (String s : strings)
		{
			char c = s.charAt(0);
			builder.append(c);
		}
		return builder.toString();
	}
	
/**
	 * Returns a shortened acronym version of a string.
	 * <p>
	 * Example:<br>
	 * {@code getAcronym2("Hello World") returns "Hllo Wrld"
	 * {@code getAcronym2("Clashsoft") returns "Clshsft"
	 * @param string
	 * @return
	 */
	public static String getAcronym2(String string)
	{
		if (string == null || string.isEmpty())
		{
			return "";
		}
		
		int len = string.length();
		StringBuilder builder = new StringBuilder(len);
		
		char prev = 0;
		char curr = 0;
		char next = string.charAt(0);
		for (int i = 1; i < len; i++)
		{
			prev = curr;
			curr = next;
			next = string.charAt(i);
			
			if (isVowel(curr) && isConsonant(prev) && isConsonant(next))
			{
				continue;
			}
			builder.append(curr);
		}
		return builder.toString();
	}
	
	public static int count(String string, char ch)
	{
		int count = 0;
		for (char c : string.toCharArray())
		{
			if (c == ch)
			{
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Cases a string.
	 * <p>
	 * Modes:
	 * <p>
	 * 0: lowercase 1: UPPERCASE 2: lower camelCase 3: Upper CamelCase 4:
	 * iNVERTED CASE 5: INVERTED LOWER CAMELcASE 6: iNVERTED uPPER cAMELcASE
	 * 
	 * @see StringUtils#firstCharToCase(String, int)
	 * @param string
	 *            the string
	 * @param mode
	 *            the mode
	 * @return the string
	 */
	public static String caseString(String string, int mode)
	{
		switch (mode)
		{
		case LOWERCASE: // lowercase
			return string.toLowerCase();
		case UPPERCASE: // UPPERCASE
			return string.toUpperCase();
		case LOWER_CAMELCASE: // lower camelCase
		{
			String[] array = string.toLowerCase().split(" ");
			for (int i = 0; i < string.length(); i++)
			{
				array[i] = firstCharToCase(array[i], LOWERCASE);
			}
			return unsplit(" ", array);
		}
		case UPPER_CAMELCASE: // Upper CamelCase
		{
			String[] array = string.toLowerCase().split(" ");
			for (int i = 0; i < string.length(); i++)
			{
				array[i] = firstCharToCase(array[i], UPPERCASE);
			}
			return unsplit(" ", array);
		}
		case INVERTED_CASE: // iNVERTED CASE
		{
			StringBuilder ret = new StringBuilder(string.length());
			for (char c : string.toCharArray())
			{
				if (Character.isUpperCase(c))
				{
					ret.append(Character.toLowerCase(c));
				}
				else
				{
					ret.append(Character.toUpperCase(c));
				}
			}
			return ret.toString();
		}
		case INVERTED_LOWER_CAMELCASE: // INVERTED LOWER CAMELcASE
			return caseString(caseString(string, 2), 4);
		case INVERTED_UPPER_CAMELCASE: // iNVERTED uPPER cAMELcASE
			return caseString(caseString(string, 3), 4);
		default:
			return string;
		}
	}
	
	/**
	 * Cases all Strings in an array. Returns a new array.
	 * 
	 * @see StringUtils#caseString(String, int)
	 * @param array
	 *            the array
	 * @param mode
	 *            the case mode
	 * @return the new array
	 */
	public static String[] caseAll(String[] array, int mode)
	{
		String[] result = new String[array.length];
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] != null)
			{
				result[i] = caseString(array[i], mode);
			}
			else
			{
				result[i] = "";
			}
		}
		return result;
	}
	
	/**
	 * Cases all Strings in a list. Returns a new list.
	 * 
	 * @see StringUtils#caseString(String, int)
	 * @param list
	 *            the list
	 * @param mode
	 *            the case mode
	 * @return the new list
	 */
	public static List<String> caseAll(List<String> list, int mode)
	{
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < list.size(); i++)
		{
			if (list.get(i) != null)
			{
				result.add(caseString(list.get(i), mode));
			}
			else
			{
				result.add("");
			}
		}
		return result;
	}
	
	/**
	 * Puts an array created with {@link String#split(String)} back together.
	 * 
	 * @param split
	 *            the string used in {@link String#split(String)}.
	 * @param parts
	 *            the output of {@link String#split(String)}
	 * @return
	 */
	public static String unsplit(String split, String... parts)
	{
		if (parts.length < 1)
		{
			return "";
		}
		
		StringBuilder result = new StringBuilder(parts.length * parts[0].length());
		for (int i = 0; i < parts.length; i++)
		{
			result.append(parts[i]);
			if (i + 1 != parts.length)
			{
				result.append(split);
			}
		}
		return result.toString();
	}
	
	/**
	 * Fastly concats an array of strings.
	 * <p>
	 * If {@code strings} is null or {@code strings} has a length of 0, this
	 * method will return an empty string.
	 * <p>
	 * If {@code strings} has a size less than three, concatening will be done
	 * using the default {@code +} operator. Otherwise, a new
	 * {@link StringBuilder} will be created to compute the result.
	 * 
	 * @see String
	 * @see StringBuilder
	 * @param strings
	 *            the array of strings
	 * @return the new string
	 */
	public static String fastConcat(String... strings)
	{
		if (strings.length < 1)
		{
			return "";
		}
		else if (strings.length < 4)
		{
			String result = strings[0];
			for (int i = 1; i < strings.length; i++)
			{
				result += strings[i];
			}
			return result;
		}
		else
		{
			StringBuilder result = new StringBuilder(strings.length * strings[0].length());
			for (String string : strings)
			{
				result.append(string);
			}
			return result.toString();
		}
	}
	
	/**
	 * Concats {@code string} with the {@code prefix} and the {@code postfix}
	 * 
	 * @param string
	 *            the string
	 * @param prefix
	 *            the prefix
	 * @param postfix
	 *            the postfix
	 * @return prefix + string + postfix
	 */
	public static String concat(String string, String prefix, String postfix)
	{
		String s;
		if (prefix != null)
		{
			s = prefix + string;
		}
		else
		{
			s = string;
		}
		if (postfix != null)
		{
			s = s + postfix;
		}
		return s;
	}
	
	/**
	 * Concats all Strings in {@code array} with the {@code prefix} and the
	 * {@code postfix}
	 * 
	 * @see StringUtils#concat(String, String, String)
	 * @param array
	 *            the array
	 * @param prefix
	 *            the prefix
	 * @param postfix
	 *            the postfix
	 * @return the new array
	 */
	public static String[] concatAll(String[] array, String prefix, String postfix)
	{
		String[] ret = new String[array.length];
		for (int i = 0; i < ret.length; i++)
		{
			ret[i] = concat(array[i], prefix, postfix);
		}
		return ret;
	}
	
	/**
	 * Concats all Strings in the list {@code list} with the {@code prefix} and
	 * the {@code postfix}
	 * 
	 * @see StringUtils#concat(String, String, String)
	 * @param list
	 *            the list
	 * @param prefix
	 *            the prefix
	 * @param postfix
	 *            the postfix
	 * @return the new list
	 */
	public static List<String> concatAll(List<String> list, String prefix, String postfix)
	{
		List<String> ret = new ArrayList<String>(list.size());
		for (int i = 0; i < ret.size(); i++)
		{
			ret.add(concat(list.get(i), prefix, postfix));
		}
		return ret;
	}
	
	/**
	 * Checks if {@code text} contains the character {@code character}.
	 * 
	 * @param string
	 *            the string
	 * @param character
	 *            the character
	 * @return true, if the string contains the character
	 */
	public static boolean contains(String string, char character)
	{
		return string.indexOf(character) != -1;
	}
	
	/**
	 * Checks if {@code text} contains the regex {@code regex}.
	 * 
	 * @see Pattern
	 * @see Matcher#find()
	 * @param string
	 *            the string
	 * @param regex
	 *            the regex
	 * @return true, if the string contains the regex
	 */
	public static boolean containsRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).find();
	}
	
	/**
	 * Checks if {@code text} contains any of the characters in {@code regex}.
	 * 
	 * @param string
	 *            the string
	 * @param regex
	 *            the regex
	 * @return true, if the string contains the any of the characters in regex
	 */
	public static boolean containsAny(String string, String regex)
	{
		return indexOfAny(string, regex) != -1;
	}
	
	/**
	 * Returns the first index of the {@code regex} in the {@code text}
	 * 
	 * @param string
	 *            the string
	 * @param integer
	 *            the integer
	 * @return the first index of the regex in the string
	 */
	public static int indexOfRegex(String string, String regex)
	{
		return Pattern.compile(regex).matcher(string).start();
	}
	
	/**
	 * Returns the first index of the any of the {@code regex} in the
	 * {@code string}
	 * 
	 * @param string
	 *            the string
	 * @param regex
	 *            the regex
	 * @return the first index of the regex in the string
	 */
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
	 * Returns the string with the first string being a) a lowercase char or b)
	 * an uppercase char
	 * <p>
	 * Modes:
	 * <p>
	 * 0: lowercase 1: Uppercase
	 * 
	 * @param string
	 *            the string
	 * @param mode
	 *            the mode
	 * @return the string
	 */
	public static String firstCharToCase(String string, int mode)
	{
		return (mode == 0 ? Character.toLowerCase(string.charAt(0)) : Character.toUpperCase(string.charAt(0))) + string.substring(1);
	}
	
	public static boolean isNumber(char c)
	{
		return c >= '0' && c <= '9';
	}
	
	public static boolean isLetter(char c)
	{
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}
	
	public static boolean isIdentifier(char c)
	{
		return isLetter(c) || isNumber(c);
	}
	
	public static boolean isSpace(char c)
	{
		return c == ' ' || c == '\t' || c == '\n' || c == '\r';
	}
	
	/**
	 * Checks the char is a vowel.
	 * 
	 * @see StringUtils#VOWELS
	 * @param c
	 *            the char
	 * @return true, if the char is a vowel
	 */
	public static boolean isVowel(char c)
	{
		return VOWELS.indexOf(Character.toLowerCase(c)) != -1;
	}
	
	/**
	 * Checks if the char is a consonant.
	 * 
	 * @see StringUtils#CONSONANTS
	 * @param c
	 *            the char
	 * @return true, if the char is a consonant
	 */
	public static boolean isConsonant(char c)
	{
		return CONSONANTS.indexOf(Character.toLowerCase(c)) != -1;
	}
	
	/**
	 * Returns a new random letter (a-z).
	 * 
	 * @see StringUtils#ALPHABET
	 * @param random
	 *            the random
	 * @return the random letter
	 */
	public static char nextLetter(Random random)
	{
		return ALPHABET.charAt(random.nextInt(ALPHABET.length()));
	}
	
	/**
	 * Returns a new random vowel (a, e, i, o, u)
	 * 
	 * @see StringUtils#VOWELS
	 * @param random
	 *            the random
	 * @return the random vowel
	 */
	public static char nextVowel(Random random)
	{
		return VOWELS.charAt(random.nextInt(VOWELS.length()));
	}
	
	/**
	 * Returns a new random consonant (b, c, d, f, g, h, j, k, l, m, n, p, q, r,
	 * s, t, v, w, x, y, z)
	 * 
	 * @see StringUtils#CONSONANTS
	 * @param random
	 *            the random
	 * @return the random consonant
	 */
	public static char nextConsonant(Random random)
	{
		return CONSONANTS.charAt(random.nextInt(CONSONANTS.length()));
	}
	
	/**
	 * Checks if char c2 can directly follow c1 in normal English.
	 * 
	 * @see StringUtils#CONSONANTCOMBINATIONS
	 * @param c1
	 *            the first char
	 * @param c2
	 *            the second char
	 * @return true, if c1 can follow c2
	 */
	public static boolean canCharFollowChar(char c1, char c2)
	{
		if (isVowel(c1) || isVowel(c2))
		{
			return true;
		}
		
		String s = new String(new char[] { Character.toLowerCase(c1), Character.toLowerCase(c2) });
		return Arrays.contains(CONSONANTCOMBINATIONS, s);
	}
}

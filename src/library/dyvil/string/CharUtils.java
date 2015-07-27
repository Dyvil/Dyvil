package dyvil.string;

import java.util.Random;

import dyvil.annotation.Utility;
import dyvil.annotation.infix;

/**
 * The {@linkplain Utility utility class} <b>CharUtils</b> can be used for
 * several character-related functions such as checking if a character is a
 * letter, a digit or a whitespace, converting it to upper- or lowercase,
 * generating a random letter, consonant or vowel among others.
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Utility(char.class)
public final class CharUtils
{
	private static final char[]	LOWER_ALPHABET	= { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u',
			'v', 'w', 'x', 'y', 'z' };
	private static final char[]	UPPER_ALPHABET	= { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z' };
	private static final char[]	LOWER_VOWELS	= { 'a', 'e', 'i', 'o', 'u' };
	private static final char[]	UPPER_VOWELS	= { 'A', 'E', 'I', 'O', 'U' };
	
	private static final char[] LOWER_CONSONANTS = { 'b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z' };
	
	private static final char[] UPPER_CONSONANTS = { 'B', 'C', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z' };
	
	private static final String[] CONSONANTS = { "bl", "br", "bs", "by", "ch", "ck", "cl", "cr", "ct", "db", "dl", "dn", "dr", "ds", "dt", "dy", "fl", "fr",
			"ft", "gh", "gl", "gn", "gr", "gs", "hd", "hl", "hr", "ht", "hy", "kl", "kn", "ks", "lc", "ld", "lf", "lk", "ls", "lt", "ly", "mb", "mn", "mp",
			"ms", "nc", "nd", "ng", "nk", "nl", "ns", "nt", "ny", "ph", "pl", "pr", "ps", "rb", "rc", "rd", "rf", "rh", "rk", "rm", "rn", "rp", "rs", "rt",
			"rw", "ry", "sh", "sk", "sl", "sn", "sp", "st", "tc", "th", "tl", "tm", "tr", "ts", "tw", "ty", "wh", "wl", "wn", "wt", "xc", "xp", "yh", "yn",
			"ys", "yt" };
			
	private CharUtils()
	{
		// no instances
	}
	
	/**
	 * Returns true if the given {@code char c} is a digit (0-9)
	 * 
	 * @see Character#isDigit(char)
	 * @param c
	 *            the character
	 * @return true, if the character is a digit
	 */
	public static @infix boolean isDigit(char c)
	{
		if (c < 128)
		{
			return c >= '0' && c <= '9';
		}
		return Character.isDigit((int) c);
	}
	
	/**
	 * Returns true if the given {@code char c} is a letter (a-zA-Z)
	 * 
	 * @see Character#isLetter(char)
	 * @param c
	 *            the character
	 * @return true, if the character is a letter
	 */
	public static @infix boolean isLetter(char c)
	{
		if (c < 128)
		{
			return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
		}
		return Character.isLetter((int) c);
	}
	
	/**
	 * Returns true if the given {@code char c} is an identifier character, i.e.
	 * a letter (as defined by {@link #isLetter(char) isLetter}), a digit (as
	 * defined by {@link #isDigit(char) isDigit}), the currency symbol '$' or
	 * the underscore '_'.
	 * 
	 * @param c
	 *            the character
	 * @return true, if the character is an identifier character
	 */
	public static @infix boolean isIdentifier(char c)
	{
		return isLetter(c) || isDigit(c) || c == '$' || c == '_';
	}
	
	/**
	 * Returns true if the given {@code char c} is a whitespace character.
	 * 
	 * @see Character#isWhitespace(char)
	 * @param c
	 *            the character
	 * @return true, if the character is a whitespace character
	 */
	public static @infix boolean isWhitespace(char c)
	{
		if (c < 128)
		{
			return c >= 0x9 && c <= 0xD || c >= 0x1C && c <= 0x20;
		}
		return Character.isWhitespace((int) c);
	}
	
	/**
	 * Returns true if the given {@code char c} is a lowercase character.
	 * 
	 * @see Character#isLowerCase(char)
	 * @param c
	 *            the character
	 * @return true, if the character is a lowercase character
	 */
	public static @infix boolean isLowerCase(char c)
	{
		if (c < 128)
		{
			return c >= 'a' && c <= 'z';
		}
		return Character.isLowerCase(c);
	}
	
	/**
	 * Returns true if the given {@code char c} is an uppercase character.
	 * 
	 * @see Character#isUpperCase(char)
	 * @param c
	 *            the character
	 * @return true, if the character is an uppercase character
	 */
	public static @infix boolean isUpperCase(char c)
	{
		if (c < 128)
		{
			return c >= 'A' && c <= 'Z';
		}
		return Character.isUpperCase(c);
	}
	
	/**
	 * Returns the lowercase representation of the given {@code char c}.
	 * 
	 * @see Character#toLowerCase(char)
	 * @param c
	 *            the character
	 * @return the lowercase representation
	 */
	public static @infix char toLowerCase(char c)
	{
		if (c >= 'A' && c <= 'Z')
		{
			return (char) (c + 32);
		}
		if (c < 128)
		{
			return c;
		}
		return (char) Character.toLowerCase((int) c);
	}
	
	/**
	 * Returns the uppercase representation of the given {@code char c}.
	 * 
	 * @see Character#toUpperCase(char)
	 * @param c
	 *            the character
	 * @return the uppercase representation
	 */
	public static @infix char toUpperCase(char c)
	{
		if (c >= 'a' && c <= 'z')
		{
			return (char) (c - 32);
		}
		if (c < 128)
		{
			return c;
		}
		return (char) Character.toUpperCase((int) c);
	}
	
	/**
	 * Returns the inverted case representation of the given {@code char c}.
	 * That is, if {@code c} is an uppercase character as defined by
	 * {@link #isUpperCase(char) isUpperCase} , the lowercase representation (as
	 * defined by {@link #toLowerCase(char) toLowerCase}), and the uppercase
	 * representation (as defined by {@link #toUpperCase(char) toUpperCase})
	 * otherwise.
	 * 
	 * @see Character#isLowerCase(char)
	 * @see Character#toUpperCase(char)
	 * @see Character#toLowerCase(char)
	 * @see Character#toUpperCase(char)
	 * @param c
	 *            the character
	 * @return the inverted case representation
	 */
	public static @infix char invertCase(char c)
	{
		if (c >= 'a' && c <= 'z')
		{
			return (char) (c - 32);
		}
		if (c >= 'A' && c <= 'Z')
		{
			return (char) (c + 32);
		}
		if (c < 256)
		{
			return c;
		}
		int ch = c;
		return (char) (Character.isUpperCase(ch) ? Character.toLowerCase(ch) : Character.toUpperCase(ch));
	}
	
	/**
	 * Returns true if the given {@code char c} is a vowel.
	 * 
	 * @param c
	 *            the character
	 * @return true, if the character is a vowel
	 */
	public static @infix boolean isVowel(char c)
	{
		// upper case
		if (c >= 'A' && c <= 'Z')
		{
			return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U';
		}
		return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u';
	}
	
	/**
	 * Returns true if the given {@code char c} is a consonant.
	 * 
	 * @param c
	 *            the character
	 * @return true, if the character is a consonant
	 */
	public static @infix boolean isConsonant(char c)
	{
		// upper case
		if (c > 'A' && c <= 'Z')
		{
			return c != 'E' && c != 'I' && c != 'O' && c != 'U';
		}
		return c > 'a' && c <= 'z' && c != 'e' && c != 'i' && c != 'o' && c != 'u';
	}
	
	/**
	 * Returns a random lowercase letter using the given {@link Random}
	 * {@code random}.
	 * 
	 * @param random
	 *            the random number generator
	 * @return the random letter
	 */
	public static @infix char nextLetter(Random random)
	{
		return LOWER_ALPHABET[random.nextInt(26)];
	}
	
	/**
	 * Returns a random lowercase letter using the given {@link Random}
	 * {@code random}.
	 * 
	 * @param random
	 *            the random number generator
	 * @return the random letter
	 */
	public static @infix char nextUppercaseLetter(Random random)
	{
		return UPPER_ALPHABET[random.nextInt(26)];
	}
	
	/**
	 * Returns a random lowercase vowel using the given {@link Random}
	 * {@code random}.
	 * 
	 * @param random
	 *            the random number generator
	 * @return the random vowel
	 */
	public static @infix char nextVowel(Random random)
	{
		return LOWER_VOWELS[random.nextInt(5)];
	}
	
	/**
	 * Returns a random uppercase vowel using the given {@link Random}
	 * {@code random}.
	 * 
	 * @param random
	 *            the random number generator
	 * @return the random vowel
	 */
	public static @infix char nextUppercaseVowel(Random random)
	{
		return UPPER_VOWELS[random.nextInt(5)];
	}
	
	/**
	 * Returns a random lowercase consonant using the given {@link Random}
	 * {@code random}.
	 * 
	 * @param random
	 *            the random number generator
	 * @return the random consonant
	 */
	public static @infix char nextConsonant(Random random)
	{
		return LOWER_CONSONANTS[random.nextInt(21)];
	}
	
	/**
	 * Returns a random uppercase consonant using the given {@link Random}
	 * {@code random}.
	 * 
	 * @param random
	 *            the random number generator
	 * @return the random consonant
	 */
	public static @infix char nextUppercaseConsonant(Random random)
	{
		return UPPER_CONSONANTS[random.nextInt(21)];
	}
	
	static boolean isCombinable(char c1, char c2)
	{
		if (c1 > 256 || c2 > 256)
		{
			return true;
		}
		if (isVowel(c1) || isVowel(c2))
		{
			return true;
		}
		
		c1 = toLowerCase(c1);
		c2 = toLowerCase(c2);
		for (String s : CONSONANTS)
		{
			if (s.charAt(0) == c1 && s.charAt(1) == c2)
			{
				return true;
			}
		}
		
		return false;
	}
}

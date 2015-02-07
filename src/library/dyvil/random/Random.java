package dyvil.random;

import dyvil.lang.annotation.infix;
import dyvil.util.StringUtils;

public class Random
{
	/**
	 * Returns true with the given chance of {@code chance}. It does that by
	 * generating a new random float using the given {@link java.util.Random
	 * Random} {@code random} and comparing it to the given {@code float chance}
	 * .
	 * 
	 * @param random
	 *            the random
	 * @param chance
	 *            the chance
	 * @return true, if a randomly generated float is less than the given chance
	 */
	public static @infix boolean nextBoolean(java.util.Random random, float chance)
	{
		return random.nextFloat() < chance;
	}
	
	public static @infix int nextInt(java.util.Random random, float f)
	{
		int i = (int) f;
		f -= i;
		if (random.nextFloat() < f)
		{
			i++;
		}
		return i;
	}
	
	public static @infix int nextInt(java.util.Random random, int min, int max)
	{
		if (min >= max)
		{
			return min;
		}
		return min + random.nextInt(max - min + 1);
	}
	
	public static @infix long nextLong(java.util.Random random, long min, long max)
	{
		if (min >= max)
		{
			return min;
		}
		return min + (random.nextLong() & max - min + 1);
	}
	
	public static @infix float nextFloat(java.util.Random random, float min, float max)
	{
		if (min >= max)
		{
			return min;
		}
		return random.nextFloat() * (max - min) + min;
	}
	
	public static @infix double nextDouble(java.util.Random random, double min, double max)
	{
		if (min >= max)
		{
			return min;
		}
		return random.nextDouble() * (max - min) + min;
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
		int len = nextInt(random, minLength, maxLength);
		StringBuilder buf = new StringBuilder(len);
		
		char prev = StringUtils.nextLetter(random);
		buf.append(Character.toUpperCase(prev));
		for (int i = 1; i < len; i++)
		{
			char c;
			
			// Always add a consonant after a vowel
			if (StringUtils.isVowel(prev))
			{
				c = StringUtils.nextConsonant(random);
			}
			else
			{
				int rnd = random.nextInt(6);
				if (rnd < 4) // Add a new consonant
				{
					c = StringUtils.nextConsonant(random);
					int i1 = 0;
					while (!StringUtils.canCharFollowChar(prev, c) && i1++ <= StringUtils.CONSONANTS.length())
					{
						c = StringUtils.nextConsonant(random);
					}
					
					if (i1 > StringUtils.CONSONANTS.length())
					{
						c = StringUtils.nextVowel(random);
					}
				}
				else
				{
					c = StringUtils.nextVowel(random); // Add a new vowel
				}
			}
			
			prev = c;
			buf.append(c);
		}
		
		return buf.toString();
	}
}

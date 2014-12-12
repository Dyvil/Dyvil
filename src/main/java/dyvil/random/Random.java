package dyvil.random;

import dyvil.lang.annotation.implicit;
import dyvil.util.StringUtils;

/**
 * The Class CSRandom.
 * <p>
 * A class that adds several randomization utils.
 * 
 * @author Clashsoft
 */
public class Random
{
	/**
	 * Returns true with the given chance.
	 * 
	 * @param random
	 *            the random
	 * @param chance
	 *            the chance
	 * @return true, if successful
	 */
	public static @implicit boolean nextBoolean(java.util.Random random, float chance)
	{
		return random.nextFloat() < chance;
	}
	
	public static @implicit int nextInt(java.util.Random random, float f)
	{
		int i = (int) f;
		f -= i;
		if (random.nextFloat() < f)
		{
			i++;
		}
		return i;
	}
	
	public static @implicit int nextInt(java.util.Random random, int min, int max)
	{
		if (min >= max)
		{
			return min;
		}
		return min + random.nextInt(max - min + 1);
	}
	
	public static @implicit long nextLong(java.util.Random random, long min, long max)
	{
		if (min >= max)
		{
			return min;
		}
		return min + (random.nextLong() & (max - min + 1));
	}
	
	public static @implicit float nextFloat(java.util.Random random, float min, float max)
	{
		if (min >= max)
		{
			return min;
		}
		return random.nextFloat() * (max - min) + min;
	}
	
	public static @implicit double nextDouble(java.util.Random random, double min, double max)
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
	public static @implicit String nextNoun(java.util.Random random, int minLength, int maxLength)
	{
		int len = nextInt(random, minLength, maxLength);
		StringBuilder buf = new StringBuilder(len);
		
		buf.append(Character.toUpperCase(StringUtils.nextLetter(random)));		
		
		for (int i = 1; i < len; i++)
		{
			int lastIndex = buf.length() - 1;
			char last = buf.charAt(lastIndex);
			char c;
			
			// Always add a consonant after a vowel
			if (StringUtils.isVowel(last))
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
					while (!StringUtils.canCharFollowChar(last, c) && i1++ <= StringUtils.CONSONANTS.length())
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
			
			buf.append(c);
		}
		
		return buf.toString();
	}
}

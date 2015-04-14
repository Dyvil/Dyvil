package dyvil.random;

import dyvil.annotation.infix;

public interface Random
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
	
	public static @infix char nextChar(java.util.Random random, String s)
	{
		return s.charAt(random.nextInt(s.length()));
	}
	
	public static @infix byte nextElement(java.util.Random random, byte[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix short nextElement(java.util.Random random, short[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix char nextElement(java.util.Random random, char[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix int nextElement(java.util.Random random, int[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix long nextElement(java.util.Random random, long[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix float nextElement(java.util.Random random, float[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix double nextElement(java.util.Random random, double[] array)
	{
		return array[random.nextInt(array.length)];
	}
	
	public static @infix <T> T nextElement(java.util.Random random, T[] array)
	{
		return array[random.nextInt(array.length)];
	}
}

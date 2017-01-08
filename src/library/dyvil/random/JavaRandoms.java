package dyvil.random;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.reflect.Modifiers;

public final class JavaRandoms
{
	private JavaRandoms()
	{
		// no instances
	}

	/**
	 * Returns true with the given chance of {@code chance}. It does that by generating a new random float using the
	 * given {@link java.util.Random Random} {@code random} and comparing it to the given {@code float chance} .
	 *
	 * @param random
	 * 	the random
	 * @param chance
	 * 	the chance
	 *
	 * @return true, if a randomly generated float is less than the given chance
	 */
	@DyvilModifiers(Modifiers.INFIX)
	public static boolean nextBoolean(java.util.@NonNull Random random, float chance)
	{
		return random.nextFloat() < chance;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int nextInt(java.util.@NonNull Random random, float f)
	{
		int i = (int) f;
		f -= i;
		if (random.nextFloat() < f)
		{
			i++;
		}
		return i;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int nextInt(java.util.@NonNull Random random, int min, int max)
	{
		if (min >= max)
		{
			return min;
		}
		return min + random.nextInt(max - min + 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long nextLong(java.util.@NonNull Random random, long min, long max)
	{
		if (min >= max)
		{
			return min;
		}
		return min + (random.nextLong() & max - min + 1);
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float nextFloat(java.util.@NonNull Random random, float min, float max)
	{
		if (min >= max)
		{
			return min;
		}
		return random.nextFloat() * (max - min) + min;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double nextDouble(java.util.@NonNull Random random, double min, double max)
	{
		if (min >= max)
		{
			return min;
		}
		return random.nextDouble() * (max - min) + min;
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char nextChar(java.util.@NonNull Random random, @NonNull String s)
	{
		return s.charAt(random.nextInt(s.length()));
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static byte nextElement(java.util.@NonNull Random random, byte @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static short nextElement(java.util.@NonNull Random random, short @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static char nextElement(java.util.@NonNull Random random, char @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static int nextElement(java.util.@NonNull Random random, int @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static long nextElement(java.util.@NonNull Random random, long @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static float nextElement(java.util.@NonNull Random random, float @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static double nextElement(java.util.@NonNull Random random, double @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}

	@DyvilModifiers(Modifiers.INFIX)
	public static <T> T nextElement(java.util.@NonNull Random random, T @NonNull [] array)
	{
		return array[random.nextInt(array.length)];
	}
}

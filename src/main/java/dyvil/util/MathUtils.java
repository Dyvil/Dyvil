package dyvil.util;

import dyvil.lang.annotation.implicit;

public class MathUtils
{
	/**
	 * Used to calculate the index of a sin value in the {@link #sinTable}.
	 * <p>
	 * Value:<br>
	 * <b>3.141592653589793D * 2D / 65536D</b>
	 */
	private static double	sinFactor		= 0.00009587379924285257D;
	
	/**
	 * Used to calculate the index of a sin value in the {@link #sinTable}.
	 * <p>
	 * Value:<br>
	 * <b>1 / sinFactor<br>
	 * 65536D / 3.141592653589793D * 2D</b>
	 */
	private static double	sinFactor2		= 10430.378350470453D;
	
	/**
	 * A table of sin values storing 65536 values between {@code 0} and
	 * {@code PI}.
	 */
	private static float[]	sinTable		= new float[65536];
	
	private static int[]	deBruijnBits	= new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9 };
	
	static
	{
		for (int i = 0; i < 65536; ++i)
		{
			sinTable[i] = (float) Math.sin(i * sinFactor);
		}
	}
	
	/**
	 * Returns the absolute value of this {@code int}.
	 * 
	 * @param i
	 * @return the abs value
	 */
	public static @implicit int abs(int i)
	{
		return i < 0 ? -i : i;
	}
	
	/**
	 * Returns the absolute value of this {@code long}.
	 * 
	 * @param l
	 * @return the abs value
	 */
	public static @implicit long abs(long l)
	{
		return l < 0 ? -l : l;
	}
	
	/**
	 * Returns the absolute value of this {@code float}.
	 * 
	 * @param f
	 * @return the abs value
	 */
	public static @implicit float abs(float f)
	{
		return f < 0F ? -f : f;
	}
	
	/**
	 * Returns the absolute value of this {@code double}.
	 * 
	 * @param d
	 * @return the abs value
	 */
	public static @implicit double abs(double d)
	{
		return d < 0D ? -d : d;
	}
	
	/**
	 * Returns the absolute value of this {@code double}.
	 * 
	 * @param i
	 * @param max
	 * @return the abs value
	 */
	public static @implicit double abs(double d, double max)
	{
		if (d < 0D)
		{
			d = -d;
		}
		if (max < 0D)
		{
			max = -max;
		}
		return d > max ? d : max;
	}
	
	public static @implicit int floor(float f)
	{
		int i = (int) f;
		return f < i ? i - 1 : i;
	}
	
	public static @implicit int floor(double d)
	{
		int i = (int) d;
		return d < i ? i - 1 : i;
	}
	
	public static @implicit int ceil(float f)
	{
		int i = (int) f;
		return f > i ? i + 1 : i;
	}
	
	public static @implicit int ceil(double d)
	{
		int i = (int) d;
		return d > i ? i + 1 : i;
	}
	
	public static @implicit int[] $dot$dot(int min, int max)
	{
		int len = max - min + 1;
		int[] arr = new int[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @implicit long[] $dot$dot(long min, long max)
	{
		int len = (int) (max - min + 1);
		long[] arr = new long[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @implicit float[] $dot$dot(float min, float max)
	{
		int len = (int) (max - min + 1);
		float[] arr = new float[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @implicit double[] $dot$dot(double min, double max)
	{
		int len = (int) (max - min + 1);
		double[] arr = new double[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @implicit float sin(float f)
	{
		return sinTable[(int) (f * sinFactor2) & 0xFFFF];
	}
	
	public static @implicit float cos(float f)
	{
		return sinTable[(int) (f * 10430.378F + 16384F) & 0xFFFF];
	}
	
	public static @implicit float tan(float f)
	{
		return sin(f) / cos(f);
	}
	
	public static @implicit double sin(double d)
	{
		return Math.sin(d);
	}
	
	public static @implicit double cos(double d)
	{
		return Math.cos(d);
	}
	
	public static @implicit double tan(double d)
	{
		return Math.tan(d);
	}
	
	public static @implicit float sqrt(float f)
	{
		return (float) Math.sqrt(f);
	}
	
	public static @implicit double sqrt(double d)
	{
		return Math.sqrt(d);
	}
	
	public static @implicit int truncate(double d)
	{
		return (int) (d + 1024D) - 1024;
	}
	
	public static @implicit int bucket(int i, int factor)
	{
		return i < 0 ? ~(~i / factor) : i / factor;
	}
	
	public static @implicit long bucket(long l, long factor)
	{
		return l < 0 ? ~(~l / factor) : l / factor;
	}
	
	public static @implicit int clamp(int i, int min, int max)
	{
		if (i <= min)
		{
			return min;
		}
		if (i >= max)
		{
			return max;
		}
		return i;
	}
	
	public static @implicit long clamp(long l, long min, long max)
	{
		if (l <= min)
		{
			return min;
		}
		if (l >= max)
		{
			return max;
		}
		return l;
	}
	
	public static @implicit float clamp(float f, float min, float max)
	{
		if (f <= min)
		{
			return min;
		}
		if (f >= max)
		{
			return max;
		}
		return f;
	}
	
	public static @implicit double clamp(double d, double min, double max)
	{
		if (d <= min)
		{
			return min;
		}
		if (d >= max)
		{
			return max;
		}
		return d;
	}
	
	public static int interpolate(int min, int max, float f)
	{
		if (f <= 0F)
		{
			return min;
		}
		if (f >= 1F)
		{
			return max;
		}
		return min + (int) ((max - min) * f);
	}

	public static long interpolate(long min, long max, double d)
	{
		if (d <= 0D)
		{
			return min;
		}
		if (d >= 1D)
		{
			return max;
		}
		return min + (long) ((max - min) * d);
	}
	
	public static float interpolate(float min, float max, float f)
	{
		if (f <= 0F)
		{
			return min;
		}
		if (f >= 1F)
		{
			return max;
		}
		return min + (max - min) * f;
	}
	
	public static double interpolate(double min, double max, double factor)
	{
		if (factor <= 0D)
		{
			return min;
		}
		if (factor >= 1D)
		{
			return max;
		}
		return min + (max - min) * factor;
	}
	
	public static @implicit int $bang(int i)
	{
		int j = i;
		while (i > 1)
		{
			j *= --i;
		}
		return j;
	}
	
	public static @implicit long $bang(long l)
	{
		long j = l;
		while (l > 1)
		{
			j *= --l;
		}
		return j;
	}
	
	public static @implicit int nPr(int n)
	{
		return $bang(n);
	}
	
	/**
	 * Returns the number of permutations of {@code n} objects when picking
	 * {@code k} at a time. The result is equal to
	 * <p>
	 * n! / (n-k)!
	 * 
	 * @param n
	 *            the total number of objects
	 * @param k
	 *            the number of objects to pick
	 * @return the number of combinations
	 */
	public static int nPr(int n, int k)
	{
		return $bang(n) / $bang(n - k);
	}
	
	/**
	 * Returns the number of combinations of {@code n} objects. This doesn't
	 * regard the order of the elements, so the result is always 1.
	 * 
	 * @param n
	 *            the number of objects
	 * @return the number of combinations
	 */
	public static int nCr(int n)
	{
		return 1;
	}
	
	/**
	 * Returns the number of combinations of {@code n} objects when picking
	 * {@code k} at a time. This doesn't regard the order of the elements. The
	 * result is equal to
	 * <p>
	 * n! / (k! * (n-k)!)
	 * 
	 * @param n
	 *            the total number of objects
	 * @param k
	 *            the number of objects to pick
	 * @return the number of combinations
	 */
	public static @implicit int nCr(int n, int k)
	{
		return $bang(n) / ($bang(k) * $bang(n - k));
	}
	
	public static @implicit float average(int[] ints)
	{
		int i = 0;
		for (int j : ints)
		{
			i += j;
		}
		return i / ints.length;
	}
	
	public static @implicit double average(long[] longs)
	{
		long l1 = 0L;
		for (long l2 : longs)
		{
			l1 += l2;
		}
		return l1 / longs.length;
	}
	
	public static @implicit float average(float[] floats)
	{
		float f1 = 0L;
		for (float f2 : floats)
		{
			f1 += f2;
		}
		return f1 / floats.length;
	}
	
	public static @implicit double average(double[] doubles)
	{
		double d1 = 0L;
		for (double d2 : doubles)
		{
			d1 += d2;
		}
		return d1 / doubles.length;
	}
	
	public static @implicit float angle(float f)
	{
		f %= 360F;
		if (f >= 180F)
		{
			f -= 360F;
		}
		if (f < -180F)
		{
			f += 360F;
		}
		return f;
	}
	
	public static @implicit double angle(double d)
	{
		d %= 360D;
		if (d >= 180D)
		{
			d -= 360D;
		}
		if (d < -180D)
		{
			d += 360D;
		}
		return d;
	}
	
	public static @implicit int parseInt(String string, int _default)
	{
		int i = _default;
		try
		{
			i = Integer.parseInt(string);
		}
		catch (Throwable t)
		{
		}
		return i;
	}
	
	public static @implicit int parseInt(String string, int radix, int _default)
	{
		int i = _default;
		try
		{
			i = Integer.parseInt(string, radix);
		}
		catch (Throwable t)
		{
		}
		return i;
	}
	
	public static @implicit long parseLong(String string, long _default)
	{
		long l = _default;
		try
		{
			l = Long.parseLong(string);
		}
		catch (Throwable t)
		{
		}
		return l;
	}
	
	public static @implicit long parseLong(String string, int radix, long _default)
	{
		long l = _default;
		try
		{
			l = Long.parseLong(string, radix);
		}
		catch (Throwable t)
		{
		}
		return l;
	}
	
	public static @implicit float parseFloat(String string, float _default)
	{
		float f = _default;
		try
		{
			f = Float.parseFloat(string);
		}
		catch (Throwable t)
		{
		}
		return f;
	}
	
	public static @implicit double parseDouble(String string, double _default)
	{
		double d = _default;
		try
		{
			d = Double.parseDouble(string);
		}
		catch (Throwable t)
		{
		}
		return d;
	}
	
	public static @implicit int powerOfTwo(int i)
	{
		int j = i - 1;
		j |= j >> 1;
		j |= j >> 2;
		j |= j >> 4;
		j |= j >> 8;
		j |= j >> 16;
		return j + 1;
	}
	
	public static @implicit boolean isPowerOfTwo(int i)
	{
		return (i & -i) != 0;
	}
	
	public static @implicit int logBaseTwo(int i)
	{
		if (isPowerOfTwo(i))
		{
			return deBruijnBits[(int) (i * 125613361L >> 27) & 0x1F];
		}
		else
		{
			return deBruijnBits[(int) (powerOfTwo(i) * 125613361L >> 27) & 0x1F] - 1;
		}
	}
	
	public static @implicit boolean checkBit(int i, byte bit)
	{
		return (i & 1 << bit) != 0;
	}
	
	public static @implicit int setBit(int i, byte bit)
	{
		return i | 1 << bit;
	}
	
	public static @implicit int clearBit(int i, byte bit)
	{
		int bitToSet = 1 << bit;
		return (i | bitToSet) ^ bitToSet;
	}
	
	public static @implicit boolean checkBit(long l, byte bit)
	{
		return (l & 1L << bit) != 0;
	}
	
	public static @implicit long setBit(long l, byte bit)
	{
		return l | 1L << bit;
	}
	
	public static @implicit long clearBit(long l, byte bit)
	{
		long bitToSet = 1L << bit;
		return (l | bitToSet) ^ bitToSet;
	}
}

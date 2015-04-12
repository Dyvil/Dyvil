package dyvil.math;

import dyvil.lang.annotation.Utility;
import dyvil.lang.annotation.infix;

/**
 * The {@linkplain Utility utility class} <b>MathUtils</b> can be used for
 * several math functions such as absolute values, min, max, clamping, ranges
 * and others. Most methods of this class are declared as {@code infix}, which
 * means they can be called on their arguments in <i>Dyvil</i> code.
 * 
 * <PRE>
 * int abs = -1 abs
 * int floor = 4.5 floor
 * float average = [ 1F, 2.3F, 4F ] average
 * [int] range = 1 .. 4
 * </PRE>
 * 
 * @author Clashsoft
 * @version 1.0
 */
@Utility({ byte.class, short.class, int.class, long.class, float.class, double.class })
public final class MathUtils
{
	/**
	 * Used to calculate the index of a sin value in the {@link #sinTable}.
	 * <p>
	 * Value:<br>
	 * <b>3.141592653589793D * 2D / 65536D</b>
	 */
	private static final double		sinFactor		= 0.00009587379924285257D;
	
	/**
	 * Used to calculate the index of a sin value in the {@link #sinTable}.
	 * <p>
	 * Value:<br>
	 * <b>1 / sinFactor<br>
	 * 65536D / 3.141592653589793D * 2D</b>
	 */
	private static final double		sinFactor2		= 10430.378350470453D;
	
	/**
	 * A table of sin values storing 65536 values between {@code 0} and
	 * {@code PI}.
	 */
	private static final float[]	sinTable		= new float[65536];
	
	private static final int[]		deBruijnBits	= new int[] { 0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26,
			12, 18, 6, 11, 5, 10, 9				};
	
	private static final int[]		sqrtTable		= new int[] { 0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57, 59, 61, 64, 65, 67, 69, 71, 73, 75,
			76, 78, 80, 81, 83, 84, 86, 87, 89, 90, 91, 93, 94, 96, 97, 98, 99, 101, 102, 103, 104, 106, 107, 108, 109, 110, 112, 113, 114, 115, 116, 117, 118,
			119, 120, 121, 122, 123, 124, 125, 126, 128, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 144, 145, 146,
			147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155, 156, 157, 158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168, 169, 170,
			170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178, 179, 180, 181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188, 189, 189, 190,
			191, 192, 192, 193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 201, 201, 202, 203, 203, 204, 204, 205, 206, 206, 207, 208, 208, 209,
			209, 210, 211, 211, 212, 212, 213, 214, 214, 215, 215, 216, 217, 217, 218, 218, 219, 219, 220, 221, 221, 222, 222, 223, 224, 224, 225, 225, 226,
			226, 227, 227, 228, 229, 229, 230, 230, 231, 231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238, 239, 240, 240, 241, 241, 242,
			242, 243, 243, 244, 244, 245, 245, 246, 246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255 };
	
	static
	{
		for (int i = 0; i < 65536; ++i)
		{
			sinTable[i] = (float) Math.sin(i * sinFactor);
		}
	}
	
	private MathUtils()
	{
	}
	
	public static int min(int a, int b)
	{
		return a <= b ? a : b;
	}
	
	public static long min(long a, long b)
	{
		return a <= b ? a : b;
	}
	
	public static float min(float a, float b)
	{
		return a <= b ? a : b;
	}
	
	public static double min(double a, double b)
	{
		return a <= b ? a : b;
	}
	
	public static int min(int a, int b, int c)
	{
		int d = a <= b ? a : b;
		return c <= d ? c : d;
	}
	
	public static long min(long a, long b, long c)
	{
		long d = a <= b ? a : b;
		return c <= d ? c : d;
	}
	
	public static float min(float a, float b, float c)
	{
		float d = a <= b ? a : b;
		return c <= d ? c : d;
	}
	
	public static double min(double a, double b, double c)
	{
		double d = a <= b ? a : b;
		return c <= d ? c : d;
	}
	
	public static int max(int a, int b, int c)
	{
		int d = a > b ? a : b;
		return c > d ? c : d;
	}
	
	public static long max(long a, long b, long c)
	{
		long d = a > b ? a : b;
		return c > d ? c : d;
	}
	
	public static float max(float a, float b, float c)
	{
		float d = a > b ? a : b;
		return c > d ? c : d;
	}
	
	public static double max(double a, double b, double c)
	{
		double d = a > b ? a : b;
		return c > d ? c : d;
	}
	
	/**
	 * Returns the absolute value of this {@code int}.
	 * 
	 * @param i
	 * @return the abs value
	 */
	public static @infix int abs(int i)
	{
		return i < 0 ? -i : i;
	}
	
	/**
	 * Returns the absolute value of this {@code long}.
	 * 
	 * @param l
	 * @return the abs value
	 */
	public static @infix long abs(long l)
	{
		return l < 0 ? -l : l;
	}
	
	/**
	 * Returns the absolute value of this {@code float}.
	 * 
	 * @param f
	 * @return the abs value
	 */
	public static @infix float abs(float f)
	{
		return f < 0F ? -f : f;
	}
	
	/**
	 * Returns the absolute value of this {@code double}.
	 * 
	 * @param d
	 * @return the abs value
	 */
	public static @infix double abs(double d)
	{
		return d < 0D ? -d : d;
	}
	
	public static @infix int floor(float f)
	{
		int i = (int) f;
		return f < i ? i - 1 : i;
	}
	
	public static @infix int floor(double d)
	{
		int i = (int) d;
		return d < i ? i - 1 : i;
	}
	
	public static @infix int ceil(float f)
	{
		int i = (int) f;
		return f > i ? i + 1 : i;
	}
	
	public static @infix int ceil(double d)
	{
		int i = (int) d;
		return d > i ? i + 1 : i;
	}
	
	public static @infix int[] $dot$dot(int min, int max)
	{
		int len = max - min + 1;
		int[] arr = new int[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @infix long[] $dot$dot(long min, long max)
	{
		int len = (int) (max - min + 1);
		long[] arr = new long[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @infix float[] $dot$dot(float min, float max)
	{
		int len = (int) (max - min + 1);
		float[] arr = new float[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @infix double[] $dot$dot(double min, double max)
	{
		int len = (int) (max - min + 1);
		double[] arr = new double[len];
		for (int i = 0; i < len; i++)
		{
			arr[i] = min + i;
		}
		return arr;
	}
	
	public static @infix float sin(float f)
	{
		return sinTable[(int) (f * sinFactor2) & 0xFFFF];
	}
	
	public static @infix float cos(float f)
	{
		return sinTable[(int) (f * 10430.378F + 16384F) & 0xFFFF];
	}
	
	public static @infix float tan(float f)
	{
		return sin(f) / cos(f);
	}
	
	public static @infix double sin(double d)
	{
		return Math.sin(d);
	}
	
	public static @infix double cos(double d)
	{
		return Math.cos(d);
	}
	
	public static @infix double tan(double d)
	{
		return Math.tan(d);
	}
	
	public static @infix byte sqrt(byte b)
	{
		if (b >= 0)
		{
			return (byte) (sqrtTable[b] >> 4);
		}
		return 0;
	}
	
	public static @infix short sqrt(short s)
	{
		int xn;
		
		if (s >= 0x100)
		{
			if (s >= 0x1000)
			{
				if (s >= 0x4000)
				{
					xn = sqrtTable[s >> 8] + 1;
				}
				else
				{
					xn = (sqrtTable[s >> 6] >> 1) + 1;
				}
			}
			else if (s >= 0x400)
			{
				xn = (sqrtTable[s >> 4] >> 2) + 1;
			}
			else
			{
				xn = (sqrtTable[s >> 2] >> 3) + 1;
			}
			
			return (short) (xn * xn > s ? --xn : xn);
		}
		else if (s >= 0)
		{
			return (short) (sqrtTable[s] >> 4);
		}
		
		return 0;
	}
	
	public static @infix int sqrt(int i)
	{
		int xn;
		
		if (i >= 0x10000)
		{
			if (i >= 0x1000000)
			{
				if (i >= 0x10000000)
				{
					if (i >= 0x40000000)
					{
						xn = sqrtTable[i >> 24] << 8;
					}
					else
					{
						xn = sqrtTable[i >> 22] << 7;
					}
				}
				if (i >= 0x4000000)
				{
					xn = sqrtTable[i >> 20] << 6;
				}
				else
				{
					xn = sqrtTable[i >> 18] << 5;
				}
				
				xn = xn + 1 + i / xn >> 1;
				xn = xn + 1 + i / xn >> 1;
				return xn * xn > i ? --xn : xn;
			}
			if (i >= 0x100000)
			{
				if (i >= 0x400000)
				{
					xn = sqrtTable[i >> 16] << 4;
				}
				else
				{
					xn = sqrtTable[i >> 14] << 3;
				}
			}
			else if (i >= 0x40000)
			{
				xn = sqrtTable[i >> 12] << 2;
			}
			else
			{
				xn = sqrtTable[i >> 10] << 1;
			}
			
			xn = xn + 1 + i / xn >> 1;
			
			return xn * xn > i ? --xn : xn;
		}
		if (i >= 0x100)
		{
			if (i >= 0x1000)
			{
				if (i >= 0x4000)
				{
					xn = sqrtTable[i >> 8] + 1;
				}
				else
				{
					xn = (sqrtTable[i >> 6] >> 1) + 1;
				}
			}
			else if (i >= 0x400)
			{
				xn = (sqrtTable[i >> 4] >> 2) + 1;
			}
			else
			{
				xn = (sqrtTable[i >> 2] >> 3) + 1;
			}
			
			return xn * xn > i ? --xn : xn;
		}
		else if (i >= 0)
		{
			return sqrtTable[i] >> 4;
		}
		
		return 0;
	}
	
	public static @infix long sqrt(long l)
	{
		return (long) Math.sqrt(l);
	}
	
	public static @infix float sqrt(float f)
	{
		return (float) Math.sqrt(f);
	}
	
	public static @infix double sqrt(double d)
	{
		return Math.sqrt(d);
	}
	
	public static @infix int truncate(double d)
	{
		return (int) (d + 1024D) - 1024;
	}
	
	public static @infix int bucket(int i, int factor)
	{
		return i < 0 ? ~(~i / factor) : i / factor;
	}
	
	public static @infix long bucket(long l, long factor)
	{
		return l < 0 ? ~(~l / factor) : l / factor;
	}
	
	public static @infix int clamp(int i, int min, int max)
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
	
	public static @infix long clamp(long l, long min, long max)
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
	
	public static @infix float clamp(float f, float min, float max)
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
	
	public static @infix double clamp(double d, double min, double max)
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
	
	public static @infix int interpolate(float f, int min, int max)
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
	
	public static @infix long interpolate(double d, long min, long max)
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
	
	public static @infix float interpolate(float f, float min, float max)
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
	
	public static @infix double interpolate(double d, double min, double max)
	{
		if (d <= 0D)
		{
			return min;
		}
		if (d >= 1D)
		{
			return max;
		}
		return min + (max - min) * d;
	}
	
	public static @infix int $bang(int v)
	{
		int j = v;
		while (v > 1)
		{
			j *= --v;
		}
		return j;
	}
	
	public static @infix long $bang(long v)
	{
		long j = v;
		while (v > 1L)
		{
			j *= --v;
		}
		return j;
	}
	
	public static @infix float $bang(float v)
	{
		float j = v;
		while (v > 1F)
		{
			j *= --v;
		}
		return j;
	}
	
	public static @infix double $bang(double v)
	{
		double j = v;
		while (v > 1D)
		{
			j *= --v;
		}
		return j;
	}
	
	public static @infix int nPr(int n)
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
	public static @infix int nPr(int n, int k)
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
	public static @infix int nCr(int n)
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
	public static @infix int nCr(int n, int k)
	{
		return $bang(n) / ($bang(k) * $bang(n - k));
	}
	
	public static @infix int sum(int[] ints)
	{
		int total = 0;
		for (int i : ints)
		{
			total += i;
		}
		return total;
	}
	
	public static @infix long sum(long[] longs)
	{
		long total = 0L;
		for (long l : longs)
		{
			total += l;
		}
		return total;
	}
	
	public static @infix float sum(float[] floats)
	{
		float total = 0L;
		for (float f : floats)
		{
			total += f;
		}
		return total;
	}
	
	public static @infix double sum(double[] doubles)
	{
		double total = 0L;
		for (double d : doubles)
		{
			total += d;
		}
		return total;
	}
	
	public static @infix float average(int[] ints)
	{
		int total = 0;
		for (int i : ints)
		{
			total += i;
		}
		return total / (float) ints.length;
	}
	
	public static @infix double average(long[] longs)
	{
		long total = 0L;
		for (long l : longs)
		{
			total += l;
		}
		return total / (double) longs.length;
	}
	
	public static @infix float average(float[] floats)
	{
		float total = 0L;
		for (float f : floats)
		{
			total += f;
		}
		return total / floats.length;
	}
	
	public static @infix double average(double[] doubles)
	{
		double total = 0L;
		for (double d : doubles)
		{
			total += d;
		}
		return total / doubles.length;
	}
	
	public static @infix float angle(float f)
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
	
	public static @infix double angle(double d)
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
	
	public static @infix int powerOfTwo(int i)
	{
		int j = i - 1;
		j |= j >> 1;
		j |= j >> 2;
		j |= j >> 4;
		j |= j >> 8;
		j |= j >> 16;
		return j + 1;
	}
	
	public static @infix boolean isPowerOfTwo(int i)
	{
		return (i & -i) != 0;
	}
	
	public static @infix int logBaseTwo(int i)
	{
		if (isPowerOfTwo(i))
		{
			return deBruijnBits[(int) (i * 125613361L >> 27) & 0x1F];
		}
		return deBruijnBits[(int) (powerOfTwo(i) * 125613361L >> 27) & 0x1F] - 1;
	}
	
	public static @infix boolean apply(int i, byte bit)
	{
		return (i & 1 << bit) != 0;
	}
	
	public static @infix int update(int i, byte bit, boolean set)
	{
		return set ? i | 1 << bit : i ^ ~(1 << bit);
	}
	
	public static @infix int setBit(int i, byte bit)
	{
		return i | 1 << bit;
	}
	
	public static @infix int clearBit(int i, byte bit)
	{
		return i ^ ~(1 << bit);
	}
	
	public static @infix boolean apply(long l, byte bit)
	{
		return (l & 1L << bit) != 0;
	}
	
	public static @infix long update(long l, byte bit, boolean set)
	{
		return set ? l | 1L << bit : l ^ ~(1L << bit);
	}
	
	public static @infix long setBit(long l, byte bit)
	{
		return l | 1L << bit;
	}
	
	public static @infix long clearBit(long l, byte bit)
	{
		return l ^ ~(1 << bit);
	}
}

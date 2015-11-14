package dyvil.random;

@FunctionalInterface
public interface Random
{
	float	FLOAT_UNIT	= 1F / (1 << 24);
	double	DOUBLE_UNIT	= 1D / (1L << 53);
	
	public static Random apply()
	{
		return new JavaBasedRandom();
	}
	
	public static Random apply(long seed)
	{
		return new JavaBasedRandom(seed);
	}
	
	public int next(int bits);
	
	public default boolean nextBoolean()
	{
		return this.next(1) > 0;
	}
	
	public default boolean nextBoolean(float f)
	{
		return this.nextFloat() < f;
	}
	
	public default byte nextByte()
	{
		return (byte) this.next(7);
	}
	
	public default byte nextByte(byte max)
	{
		return (byte) this.nextInt(max);
	}
	
	public default byte nextByte(byte min, byte max)
	{
		return (byte) (min + this.nextInt(max - min + 1));
	}
	
	public default short nextShort()
	{
		return (short) this.next(15);
	}
	
	public default short nextShort(short max)
	{
		return (short) this.nextInt(max);
	}
	
	public default short nextShort(short min, short max)
	{
		return (short) (min + this.nextInt(max - min + 1));
	}
	
	public default char nextChar()
	{
		return (char) this.next(16);
	}
	
	public default char nextChar(char max)
	{
		return (char) this.nextInt(max);
	}
	
	public default char nextChar(char min, char max)
	{
		return (char) (min + this.nextInt(max - min + 1));
	}
	
	public default int nextInt()
	{
		return this.next(31);
	}
	
	public default int nextInt(int max)
	{
		int r = next(31);
		int m = max - 1;
		if ((max & m) == 0)
		{
			return (int) (max * (long) r >> 31);
		}
		for (int u = r; u - (r = u % max) + m < 0; u = next(31))
		{
		}
		return r;
	}
	
	public default int nextInt(int min, int max)
	{
		return min + this.nextInt(max - min + 1);
	}
	
	public default int nextInt(float f)
	{
		return (int) f + (this.nextFloat() < f ? 0 : 1);
	}
	
	public default long nextLong()
	{
		return ((long) next(32) << 32) + next(32);
	}
	
	public default long nextLong(long max)
	{
		long r = this.nextLong();
		long m = max - 1;
		if ((max & m) == 0L)
		{
			return r & m;
		}
		for (long u = r >>> 1; u + m - (r = u % max) < 0L; u = this.nextLong() >>> 1)
		{
		}
		return r;
	}
	
	public default long nextLong(long min, long max)
	{
		return min + this.nextLong(max - min + 1);
	}
	
	public default float nextFloat()
	{
		return next(24) * FLOAT_UNIT;
	}
	
	public default float nextFloat(float max)
	{
		return this.nextFloat() * max;
	}
	
	public default float nextFloat(float min, float max)
	{
		return min + this.nextFloat() * (max - min);
	}
	
	public default double nextDouble()
	{
		return (((long) next(26) << 27) + next(27)) * DOUBLE_UNIT;
	}
	
	public default double nextDouble(double max)
	{
		return this.nextDouble() * max;
	}
	
	public default double nextDouble(double min, double max)
	{
		return min + this.nextDouble() * (max - min);
	}
	
	public default double nextGaussian()
	{
		// warning: requires override to function properly
		return this.nextDouble() * 2D - 1D;
	}
}

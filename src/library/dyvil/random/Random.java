package dyvil.random;

@FunctionalInterface
public interface Random
{
	float  FLOAT_UNIT  = 1F / (1 << 24);
	double DOUBLE_UNIT = 1D / (1L << 53);
	
	static Random apply()
	{
		return new JavaBasedRandom();
	}
	
	static Random apply(long seed)
	{
		return new JavaBasedRandom(seed);
	}
	
	int next(int bits);
	
	default boolean nextBoolean()
	{
		return this.next(1) > 0;
	}
	
	default boolean nextBoolean(float f)
	{
		return this.nextFloat() < f;
	}
	
	default byte nextByte()
	{
		return (byte) this.next(7);
	}
	
	default byte nextByte(byte max)
	{
		return (byte) this.nextInt(max);
	}
	
	default byte nextByte(byte min, byte max)
	{
		return (byte) (min + this.nextInt(max - min + 1));
	}
	
	default short nextShort()
	{
		return (short) this.next(15);
	}
	
	default short nextShort(short max)
	{
		return (short) this.nextInt(max);
	}
	
	default short nextShort(short min, short max)
	{
		return (short) (min + this.nextInt(max - min + 1));
	}
	
	default char nextChar()
	{
		return (char) this.next(16);
	}
	
	default char nextChar(char max)
	{
		return (char) this.nextInt(max);
	}
	
	default char nextChar(char min, char max)
	{
		return (char) (min + this.nextInt(max - min + 1));
	}
	
	default int nextInt()
	{
		return this.next(31);
	}
	
	default int nextInt(int max)
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
	
	default int nextInt(int min, int max)
	{
		return min + this.nextInt(max - min + 1);
	}
	
	default int nextInt(float f)
	{
		return (int) f + (this.nextFloat() < f ? 0 : 1);
	}
	
	default long nextLong()
	{
		return ((long) next(32) << 32) + next(32);
	}
	
	default long nextLong(long max)
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
	
	default long nextLong(long min, long max)
	{
		return min + this.nextLong(max - min + 1);
	}
	
	default float nextFloat()
	{
		return next(24) * FLOAT_UNIT;
	}
	
	default float nextFloat(float max)
	{
		return this.nextFloat() * max;
	}
	
	default float nextFloat(float min, float max)
	{
		return min + this.nextFloat() * (max - min);
	}
	
	default double nextDouble()
	{
		return (((long) next(26) << 27) + next(27)) * DOUBLE_UNIT;
	}
	
	default double nextDouble(double max)
	{
		return this.nextDouble() * max;
	}
	
	default double nextDouble(double min, double max)
	{
		return min + this.nextDouble() * (max - min);
	}
	
	default double nextGaussian()
	{
		// warning: requires override to function properly
		return this.nextDouble() * 2D - 1D;
	}
}

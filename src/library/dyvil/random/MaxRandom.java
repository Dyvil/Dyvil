package dyvil.random;

import java.util.Random;

import dyvil.math.MathUtils;

/**
 * A {@link Random} implementation that always returns the maximum value.
 * 
 * @author Clashsoft
 */
public class MaxRandom extends Random implements DRandom
{
	private static final long		serialVersionUID	= -6067026546099361014L;
	
	public static final MaxRandom	instance			= new MaxRandom();
	
	private MaxRandom()
	{
	}
	
	@Override
	public int next(int bits)
	{
		return (1 << bits + 1) - 1;
	}
	
	@Override
	public void nextBytes(byte[] bytes)
	{
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = Byte.MAX_VALUE;
		}
	}
	
	@Override
	public boolean nextBoolean()
	{
		return true;
	}
	
	@Override
	public boolean nextBoolean(float f)
	{
		return true;
	}
	
	@Override
	public int nextInt(int max)
	{
		return max - 1;
	}
	
	@Override
	public int nextInt(float f)
	{
		return MathUtils.ceil(f);
	}
	
	@Override
	public int nextInt(int min, int max)
	{
		return max;
	}
	
	@Override
	public long nextLong()
	{
		return 1L;
	}
	
	@Override
	public long nextLong(long max)
	{
		return max - 1;
	}
	
	@Override
	public long nextLong(long min, long max)
	{
		return max - 1;
	}
	
	@Override
	public float nextFloat()
	{
		return 1F;
	}
	
	@Override
	public float nextFloat(float min, float max)
	{
		return max;
	}
	
	@Override
	public double nextDouble()
	{
		return 1D;
	}
	
	@Override
	public double nextDouble(double min, double max)
	{
		return max;
	}
	
	@Override
	public synchronized double nextGaussian()
	{
		return 1D;
	}
}

package dyvil.random;

import java.util.Random;

import dyvil.util.MathUtils;

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
	protected int next(int bits)
	{
		return bits;
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
	public int nextInt(int n)
	{
		return n - 1;
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
	public long nextLong(long min, long max)
	{
		return max;
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

package dyvil.random;

import java.util.Random;

/**
 * A {@link Random} implementation that always returns the minimum value.
 * 
 * @author Clashsoft
 */
public class MinRandom extends Random implements DRandom
{
	private static final long		serialVersionUID	= 4703419198212688268L;
	
	public static final MinRandom	instance			= new MinRandom();
	
	private MinRandom()
	{
	}
	
	@Override
	public int next(int bits)
	{
		return 0;
	}
	
	@Override
	public void nextBytes(byte[] bytes)
	{
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = Byte.MIN_VALUE;
		}
	}
	
	@Override
	public boolean nextBoolean()
	{
		return false;
	}
	
	@Override
	public boolean nextBoolean(float f)
	{
		return false;
	}
	
	@Override
	public int nextInt(int max)
	{
		return 0;
	}
	
	@Override
	public int nextInt(float f)
	{
		return 0;
	}
	
	@Override
	public int nextInt(int min, int max)
	{
		return min;
	}
	
	@Override
	public long nextLong()
	{
		return 0L;
	}
	
	@Override
	public long nextLong(long max)
	{
		return 0;
	}
	
	@Override
	public long nextLong(long min, long max)
	{
		return min;
	}
	
	@Override
	public float nextFloat()
	{
		return 0F;
	}
	
	@Override
	public float nextFloat(float min, float max)
	{
		return min;
	}
	
	@Override
	public double nextDouble()
	{
		return 0D;
	}
	
	@Override
	public double nextDouble(double min, double max)
	{
		return min;
	}
	
	@Override
	public synchronized double nextGaussian()
	{
		return 0D;
	}
}

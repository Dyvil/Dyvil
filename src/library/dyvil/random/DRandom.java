package dyvil.random;

public interface DRandom
{
	public boolean nextBoolean();
	
	public boolean nextBoolean(float f);
	
	public int next(int bits);
	
	public int nextInt();
	
	public int nextInt(int max);
	
	public int nextInt(float f);
	
	public int nextInt(int min, int max);
	
	public long nextLong();
	
	public long nextLong(long max);
	
	public long nextLong(long min, long max);
	
	public float nextFloat();
	
	public float nextFloat(float min, float max);
	
	public double nextDouble();
	
	public double nextDouble(double min, double max);
	
	public double nextGaussian();
}

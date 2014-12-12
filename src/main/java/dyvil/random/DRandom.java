package dyvil.random;

public interface DRandom
{
	public boolean nextBoolean();
	
	public boolean nextBoolean(float f);
	
	public int nextInt(int n);
	
	public int nextInt(float f);
	
	public int nextInt(int min, int max);
	
	public long nextLong();
	
	public long nextLong(long min, long max);
	
	public float nextFloat();
	
	public float nextFloat(float min, float max);
	
	public double nextDouble();
	
	public double nextDouble(double min, double max);
	
	public double nextGaussian();
}

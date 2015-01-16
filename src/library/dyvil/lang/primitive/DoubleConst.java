package dyvil.lang.primitive;

import dyvil.lang.Double;

public class DoubleConst extends dyvil.lang.Double
{
	protected DoubleConst(double value)
	{
		super(value);
	}
	
	public static DoubleConst get(double value)
	{
		return ConstPool.getDouble(value);
	}
	
	@Override
	public Double $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(short v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(char v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(int v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(long v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(float v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(double v)
	{
		return get(v);
	}
}

package dyvil.lang.primitive;

import dyvil.lang.Number;

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
	public Number $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(short v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(char v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(int v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(long v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(float v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(double v)
	{
		return get(v);
	}
}

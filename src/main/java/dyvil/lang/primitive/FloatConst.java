package dyvil.lang.primitive;

import dyvil.lang.Number;

public class FloatConst extends dyvil.lang.Float
{
	protected FloatConst(float value)
	{
		super(value);
	}
	
	public static FloatConst get(float value)
	{
		return ConstPool.getFloat(value);
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
		return DoubleConst.get(v);
	}
}

package dyvil.lang.primitive;

import dyvil.lang.Double;
import dyvil.lang.Float;

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
	public Float $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Float $eq(short v)
	{
		return get(v);
	}
	
	@Override
	public Float $eq(char v)
	{
		return get(v);
	}
	
	@Override
	public Float $eq(int v)
	{
		return get(v);
	}
	
	@Override
	public Float $eq(long v)
	{
		return get(v);
	}
	
	@Override
	public Float $eq(float v)
	{
		return get(v);
	}
	
	@Override
	public Double $eq(double v)
	{
		return DoubleConst.get(v);
	}
}

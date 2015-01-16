package dyvil.lang.primitive;

import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Int;
import dyvil.lang.Long;

public class IntConst extends dyvil.lang.Int
{
	protected IntConst(int value)
	{
		super(value);
	}
	
	public static IntConst get(int value)
	{
		return ConstPool.getInt(value);
	}
	
	@Override
	public Int $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Int $eq(short v)
	{
		return get(v);
	}
	
	@Override
	public Int $eq(char v)
	{
		return get(v);
	}
	
	@Override
	public Int $eq(int v)
	{
		return get(v);
	}
	
	@Override
	public Long $eq(long v)
	{
		return LongConst.get(v);
	}
	
	@Override
	public Float $eq(float v)
	{
		return FloatConst.get(v);
	}
	
	@Override
	public Double $eq(double v)
	{
		return DoubleConst.get(v);
	}
}

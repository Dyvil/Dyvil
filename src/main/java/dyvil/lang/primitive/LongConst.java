package dyvil.lang.primitive;

import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;

public class LongConst extends dyvil.lang.Long
{
	protected LongConst(long value)
	{
		super(value);
	}
	
	public static LongConst get(long value)
	{
		return ConstPool.getLong(value);
	}
	
	@Override
	public Long $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Long $eq(short v)
	{
		return get(v);
	}
	
	@Override
	public Long $eq(char v)
	{
		return get(v);
	}
	
	@Override
	public Long $eq(int v)
	{
		return get(v);
	}
	
	@Override
	public Long $eq(long v)
	{
		return get(v);
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

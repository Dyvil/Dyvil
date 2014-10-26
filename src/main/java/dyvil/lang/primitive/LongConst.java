package dyvil.lang.primitive;

import dyvil.lang.Number;

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
		return FloatConst.get(v);
	}
	
	@Override
	public Number $eq(double v)
	{
		return DoubleConst.get(v);
	}
}

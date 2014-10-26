package dyvil.lang.primitive;

import dyvil.lang.Number;

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
		return LongConst.get(v);
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

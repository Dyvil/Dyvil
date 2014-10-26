package dyvil.lang.primitive;

import dyvil.lang.Number;

public class ShortConst extends dyvil.lang.Short
{
	protected ShortConst(short value)
	{
		super(value);
	}
	
	public static ShortConst get(short value)
	{
		return ConstPool.getShort(value);
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
		return CharConst.get(v);
	}
	
	@Override
	public Number $eq(int v)
	{
		return IntConst.get(v);
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

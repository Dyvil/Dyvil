package dyvil.lang.primitive;

import dyvil.lang.Number;

public class CharConst extends dyvil.lang.Char
{
	protected CharConst(char value)
	{
		super(value);
	}
	
	public static CharConst get(char value)
	{
		return ConstPool.getChar(value);
	}
	
	@Override
	public Number $eq(byte v)
	{
		return get((char) v);
	}
	
	@Override
	public Number $eq(short v)
	{
		return get((char) v);
	}
	
	@Override
	public Number $eq(char v)
	{
		return get(v);
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

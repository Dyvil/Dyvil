package dyvil.lang.primitive;

import dyvil.lang.Number;

public class ByteConst extends dyvil.lang.Byte
{
	protected ByteConst(byte value)
	{
		super(value);
	}
	
	public static ByteConst get(byte value)
	{
		return ConstPool.getByte(value);
	}
	
	@Override
	public Number $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Number $eq(short v)
	{
		return ShortConst.get(v);
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

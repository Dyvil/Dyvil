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
	public Number set$(byte v)
	{
		return get(v);
	}
	
	@Override
	public Number set$(short v)
	{
		return ShortConst.get(v);
	}
	
	@Override
	public Number set$(char v)
	{
		return CharConst.get(v);
	}
	
	@Override
	public Number set$(int v)
	{
		return IntConst.get(v);
	}
	
	@Override
	public Number set$(long v)
	{
		return LongConst.get(v);
	}
	
	@Override
	public Number set$(float v)
	{
		return FloatConst.get(v);
	}
	
	@Override
	public Number set$(double v)
	{
		return DoubleConst.get(v);
	}
}

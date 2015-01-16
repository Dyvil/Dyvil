package dyvil.lang.primitive;

import dyvil.lang.*;
import dyvil.lang.Byte;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;
import dyvil.lang.Short;

public class ByteConst extends Byte
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
	public Byte $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Short $eq(short v)
	{
		return ShortConst.get(v);
	}
	
	@Override
	public Char $eq(char v)
	{
		return CharConst.get(v);
	}
	
	@Override
	public Int $eq(int v)
	{
		return IntConst.get(v);
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

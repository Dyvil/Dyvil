package dyvil.lang.primitive;

import dyvil.lang.*;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;
import dyvil.lang.Short;

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
	public Short $eq(byte v)
	{
		return get(v);
	}
	
	@Override
	public Short $eq(short v)
	{
		return get(v);
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

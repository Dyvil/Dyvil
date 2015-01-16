package dyvil.lang.primitive;

import dyvil.lang.*;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;

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
	public Char $eq(byte v)
	{
		return get((char) v);
	}
	
	@Override
	public Char $eq(short v)
	{
		return get((char) v);
	}
	
	@Override
	public Char $eq(char v)
	{
		return get(v);
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

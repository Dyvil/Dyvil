package dyvil.lang.primitive;

import dyvil.lang.*;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;

public class CharRef extends dyvil.lang.Char
{
	protected CharRef(char value)
	{
		super(value);
	}
	
	public static final CharRef get(char value)
	{
		return new CharRef(value);
	}
	
	@Override
	public Char $eq(byte v)
	{
		this.value = (char) v;
		return this;
	}
	
	@Override
	public Char $eq(short v)
	{
		this.value = (char) v;
		return this;
	}
	
	@Override
	public Char $eq(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Int $eq(int v)
	{
		return IntRef.get(v);
	}
	
	@Override
	public Long $eq(long v)
	{
		return LongRef.get(v);
	}
	
	@Override
	public Float $eq(float v)
	{
		return FloatRef.get(v);
	}
	
	@Override
	public Double $eq(double v)
	{
		return DoubleRef.get(v);
	}
}

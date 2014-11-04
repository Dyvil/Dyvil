package dyvil.lang.primitive;

import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Int;
import dyvil.lang.Long;

public class IntRef extends dyvil.lang.Int
{
	protected IntRef(int value)
	{
		super(value);
	}
	
	public static final IntRef get(int value)
	{
		return new IntRef(value);
	}
	
	@Override
	public Int $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Int $eq(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Int $eq(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Int $eq(int v)
	{
		this.value = v;
		return this;
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

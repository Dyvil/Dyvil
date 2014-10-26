package dyvil.lang.primitive;

import dyvil.lang.Number;

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
	public Number $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number $eq(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number $eq(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number $eq(int v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Number $eq(long v)
	{
		return LongRef.get(v);
	}
	
	@Override
	public Number $eq(float v)
	{
		return FloatRef.get(v);
	}
	
	@Override
	public Number $eq(double v)
	{
		return DoubleRef.get(v);
	}
}

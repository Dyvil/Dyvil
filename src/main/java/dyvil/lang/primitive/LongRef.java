package dyvil.lang.primitive;

import dyvil.lang.Number;

public class LongRef extends dyvil.lang.Long
{
	protected LongRef(long value)
	{
		super(value);
	}
	
	public static final LongRef get(long value)
	{
		return new LongRef(value);
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
		this.value = v;
		return this;
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

package dyvil.lang.primitive;

import dyvil.lang.Number;

public class FloatRef extends dyvil.lang.Float
{
	protected FloatRef(float value)
	{
		super(value);
	}
	
	public static final FloatRef get(float value)
	{
		return new FloatRef(value);
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
		this.value = v;
		return this;
	}
	
	@Override
	public Number $eq(double v)
	{
		return DoubleRef.get(v);
	}
}

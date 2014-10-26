package dyvil.lang.primitive;

import dyvil.lang.Number;

public class DoubleRef extends dyvil.lang.Double
{
	protected DoubleRef(double value)
	{
		super(value);
	}
	
	public static final DoubleRef get(double value)
	{
		return new DoubleRef(value);
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
		this.value = v;
		return this;
	}
}

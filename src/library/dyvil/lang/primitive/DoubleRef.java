package dyvil.lang.primitive;

import dyvil.lang.Double;

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
	public Double $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(int v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(long v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(float v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(double v)
	{
		this.value = v;
		return this;
	}
}

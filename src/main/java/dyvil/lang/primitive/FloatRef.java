package dyvil.lang.primitive;

import dyvil.lang.Double;
import dyvil.lang.Float;

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
	public Float $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Float $eq(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Float $eq(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Float $eq(int v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Float $eq(long v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Float $eq(float v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Double $eq(double v)
	{
		return DoubleRef.get(v);
	}
}

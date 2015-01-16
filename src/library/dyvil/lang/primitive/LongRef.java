package dyvil.lang.primitive;

import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;

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
	public Long $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Long $eq(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Long $eq(char v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Long $eq(int v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Long $eq(long v)
	{
		this.value = v;
		return this;
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

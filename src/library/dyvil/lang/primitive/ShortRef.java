package dyvil.lang.primitive;

import dyvil.lang.*;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;
import dyvil.lang.Short;

public class ShortRef extends dyvil.lang.Short
{
	protected ShortRef(short value)
	{
		super(value);
	}
	
	public static final ShortRef get(short value)
	{
		return new ShortRef(value);
	}
	
	@Override
	public Short $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Short $eq(short v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Char $eq(char v)
	{
		return CharRef.get(v);
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

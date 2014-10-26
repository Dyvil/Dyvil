package dyvil.lang.primitive;

import dyvil.lang.Number;

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
		return CharRef.get(v);
	}
	
	@Override
	public Number $eq(int v)
	{
		return IntRef.get(v);
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

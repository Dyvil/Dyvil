package dyvil.lang.primitive;

import dyvil.lang.Number;

public class CharRef extends dyvil.lang.Char
{
	protected CharRef(char value)
	{
		super(value);
	}
	
	public static final CharRef get(char value)
	{
		return new CharRef(value);
	}
	
	@Override
	public Number $eq(byte v)
	{
		this.value = (char) v;
		return this;
	}
	
	@Override
	public Number $eq(short v)
	{
		this.value = (char) v;
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

package dyvil.lang.primitive;

import dyvil.lang.Number;

public class ByteRef extends dyvil.lang.Byte
{
	protected ByteRef(byte value)
	{
		super(value);
	}
	
	public static final ByteRef get(byte value)
	{
		return new ByteRef(value);
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
		return ShortRef.get(v);
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

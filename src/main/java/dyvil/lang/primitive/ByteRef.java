package dyvil.lang.primitive;

import dyvil.lang.*;
import dyvil.lang.Byte;
import dyvil.lang.Double;
import dyvil.lang.Float;
import dyvil.lang.Long;
import dyvil.lang.Short;

public class ByteRef extends Byte
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
	public Byte $eq(byte v)
	{
		this.value = v;
		return this;
	}
	
	@Override
	public Short $eq(short v)
	{
		return ShortRef.get(v);
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

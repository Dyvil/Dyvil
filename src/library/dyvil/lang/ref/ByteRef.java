package dyvil.lang.ref;

import dyvil.lang.literal.IntConvertible;

@IntConvertible
public class ByteRef implements ByteRef$
{
	public byte	value;
	
	public static ByteRef apply(byte value)
	{
		return new ByteRef(value);
	}
	
	public ByteRef(byte value)
	{
		this.value = value;
	}
	
	@Override
	public byte apply()
	{
		return this.value;
	}
	
	@Override
	public void update(byte value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Byte.toString(this.value);
	}
}

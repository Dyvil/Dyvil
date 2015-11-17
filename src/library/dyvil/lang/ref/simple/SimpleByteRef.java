package dyvil.lang.ref.simple;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.ref.ByteRef;

@IntConvertible
public class SimpleByteRef implements ByteRef
{
	public byte value;
	
	public static SimpleByteRef apply(byte value)
	{
		return new SimpleByteRef(value);
	}
	
	public SimpleByteRef(byte value)
	{
		this.value = value;
	}
	
	@Override
	public byte get()
	{
		return this.value;
	}
	
	@Override
	public void set(byte value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Byte.toString(this.value);
	}
}

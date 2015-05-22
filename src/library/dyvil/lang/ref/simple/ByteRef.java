package dyvil.lang.ref.simple;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.ref.IByteRef;

@IntConvertible
public class ByteRef implements IByteRef
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

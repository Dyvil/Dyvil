package dyvil.ref.simple;

import dyvil.lang.LiteralConvertible;
import dyvil.ref.ByteRef;

@LiteralConvertible.FromInt
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
}

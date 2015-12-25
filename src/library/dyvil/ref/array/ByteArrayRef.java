package dyvil.ref.array;

import dyvil.ref.ByteRef;

public class ByteArrayRef implements ByteRef
{
	protected final byte[] array;
	protected final int    index;
	
	public ByteArrayRef(byte[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public byte get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(byte value)
	{
		this.array[this.index] = value;
	}
}

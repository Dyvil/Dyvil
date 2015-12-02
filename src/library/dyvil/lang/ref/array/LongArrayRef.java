package dyvil.lang.ref.array;

import dyvil.lang.ref.LongRef;

public class LongArrayRef implements LongRef
{
	protected final long[] array;
	protected final int    index;
	
	public LongArrayRef(long[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public long get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(long value)
	{
		this.array[this.index] = value;
	}
}

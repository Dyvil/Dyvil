package dyvil.lang.ref.array;

import dyvil.lang.ref.IBooleanRef;

public class BooleanArrayRef implements IBooleanRef
{
	protected final boolean[]	array;
	protected final int			index;
	
	public BooleanArrayRef(boolean[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public boolean get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(boolean value)
	{
		this.array[this.index] = value;
	}
}

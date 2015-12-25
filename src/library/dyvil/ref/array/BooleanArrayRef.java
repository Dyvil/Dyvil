package dyvil.ref.array;

import dyvil.ref.BooleanRef;
import dyvil.ref.BooleanRef;

public class BooleanArrayRef implements BooleanRef
{
	protected final boolean[] array;
	protected final int       index;
	
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

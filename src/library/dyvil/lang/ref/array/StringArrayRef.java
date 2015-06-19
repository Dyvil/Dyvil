package dyvil.lang.ref.array;

import dyvil.lang.ref.StringRef;

public class StringArrayRef implements StringRef
{
	protected final String[]	array;
	protected final int			index;
	
	public StringArrayRef(String[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public String get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(String value)
	{
		this.array[this.index] = value;
	}
}

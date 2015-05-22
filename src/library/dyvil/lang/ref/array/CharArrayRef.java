package dyvil.lang.ref.array;

import dyvil.lang.ref.ICharRef;

public class CharArrayRef implements ICharRef
{
	protected final char[]	array;
	protected final int		index;
	
	public CharArrayRef(char[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public char get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(char value)
	{
		this.array[this.index] = value;
	}
}

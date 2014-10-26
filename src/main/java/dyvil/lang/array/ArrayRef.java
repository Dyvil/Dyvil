package dyvil.lang.array;

import dyvil.lang.Array;

public class ArrayRef extends Array
{
	public ArrayRef(Object[] data)
	{
		super(data);
	}
	
	public ArrayRef(int size)
	{
		super(size);
	}
	
	private ArrayRef(Class type, int size)
	{
		super(type, size);
	}
	
	public static ArrayRef get(Object[] data)
	{
		return new ArrayRef(data);
	}
	
	@Override
	public Array $eq(Object[] data)
	{
		this.data = data;
		return this;
	}
}

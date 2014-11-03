package dyvil.lang.array;

import dyvil.lang.Array;

public class ArrayConst extends Array
{
	public ArrayConst(Object[] data)
	{
		super(data);
	}
	
	public ArrayConst(int size)
	{
		super(size);
	}
	
	private ArrayConst(Class type, int size)
	{
		super(type, size);
	}
	
	public static ArrayConst get(Object[] data)
	{
		return new ArrayConst(data);
	}
	
	@Override
	public Array $eq(Object[] data)
	{
		return get(data);
	}
}

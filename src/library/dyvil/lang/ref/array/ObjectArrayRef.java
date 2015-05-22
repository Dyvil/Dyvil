package dyvil.lang.ref.array;

import dyvil.lang.ref.IObjectRef;

public class ObjectArrayRef<T> implements IObjectRef<T>
{
	protected final T[]	array;
	protected final int	index;
	
	public ObjectArrayRef(T[] array, int index)
	{
		this.array = array;
		this.index = index;
	}
	
	@Override
	public T get()
	{
		return this.array[this.index];
	}
	
	@Override
	public void set(T value)
	{
		this.array[this.index] = value;
	}
}

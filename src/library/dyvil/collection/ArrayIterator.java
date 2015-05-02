package dyvil.collection;

import java.util.Iterator;

public final class ArrayIterator<E> implements Iterator<E>
{
	private int	index;
	private E[]	array;
	private int	size;
	
	public ArrayIterator(E[] array)
	{
		this.array = array;
		this.size = array.length;
	}
	
	public ArrayIterator(E[] array, int size)
	{
		this.array = array;
		this.size = size;
	}
	
	public ArrayIterator(E[] array, int index, int size)
	{
		this.array = array;
		this.index = index;
		this.size = size;
	}
	
	@Override
	public E next()
	{
		return this.array[this.index++];
	}
	
	@Override
	public boolean hasNext()
	{
		return this.index < this.size;
	}
	
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}

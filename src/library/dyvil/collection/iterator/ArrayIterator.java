package dyvil.collection.iterator;

import dyvil.annotation.Immutable;
import dyvil.annotation.Mutating;
import dyvil.array.ObjectArray;
import dyvil.lang.LiteralConvertible;
import dyvil.util.ImmutableException;

import java.util.Iterator;

@LiteralConvertible.FromArray
@Immutable
public class ArrayIterator<E> implements Iterator<E>
{
	private       int index;
	private final E[] array;
	private final int size;
	
	@SafeVarargs
	public static <E> ArrayIterator<E> apply(E... array)
	{
		return new ArrayIterator<>(array);
	}
	
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
	@Mutating
	public void remove()
	{
		throw new ImmutableException();
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder("ArrayIterator(array: ");
		ObjectArray.deepToString(this.array);
		return builder.append(", index: ").append(this.index).append(", end: ").append(this.size).append(')')
		              .toString();
	}
}

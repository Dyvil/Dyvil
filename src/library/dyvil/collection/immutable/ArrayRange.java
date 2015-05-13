package dyvil.collection.immutable;

import java.util.Iterator;

import dyvil.collection.ArrayIterator;
import dyvil.lang.Ordered;
import dyvil.lang.Range;
import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
public class ArrayRange<T extends Ordered<T>> implements Range<T>
{
	protected final Ordered[]	array;
	protected final int			size;
	
	public static <T extends Ordered<T>> ArrayRange<T> apply(T first, T last)
	{
		return new ArrayRange(first, last);
	}
	
	public ArrayRange(T first, T last)
	{
		int size = first.distanceTo(last);
		if (size >= 0)
		{
			this.array = new Ordered[size];
			this.size = size;
			
			int index = 0;
			for (T current = first; current.$lt$eq(last); current = current.next())
			{
				this.array[index++] = current;
			}
			
			return;
		}
		
		size = 0;
		Ordered[] array = new Ordered[3];
		for (T current = first; current.$lt$eq(last); current = current.next())
		{
			int i = size++;
			if (i >= array.length)
			{
				Ordered[] temp = new Ordered[size];
				System.arraycopy(array, 0, temp, 0, array.length);
				array = temp;
			}
			
			array[i] = current;
		}
		
		this.array = array;
		this.size = size;
	}
	
	@Override
	public T first()
	{
		return (T) this.array[0];
	}
	
	@Override
	public T last()
	{
		return (T) this.array[this.size - 1];
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return new ArrayIterator<T>((T[]) this.array, this.size);
	}
}

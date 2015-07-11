package dyvil.collection.range;

import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.lang.Ordered;
import dyvil.lang.literal.TupleConvertible;

import dyvil.collection.Range;
import dyvil.collection.iterator.ArrayIterator;

@TupleConvertible
public class ArrayRange<T extends Ordered<T>> implements Range<T>
{
	protected final Ordered[]	array;
	protected final int			count;
	
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
			this.count = size;
			
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
		this.count = size;
	}
	
	private ArrayRange(Ordered[] array, int count)
	{
		this.array = array;
		this.count = count;
	}
	
	@Override
	public T first()
	{
		return (T) this.array[0];
	}
	
	@Override
	public T last()
	{
		return (T) this.array[this.count - 1];
	}
	
	@Override
	public int count()
	{
		return this.count;
	}
	
	@Override
	public int estimateCount()
	{
		return this.count;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return new ArrayIterator<T>((T[]) this.array, this.count);
	}
	
	@Override
	public void forEach(Consumer<? super T> action)
	{
		for (int i = 0; i < this.count; i++)
		{
			action.accept((T) this.array[i]);
		}
	}
	
	@Override
	public boolean $qmark(Object o)
	{
		for (int i = 0; i < this.count; i++)
		{
			if (this.array[i].$eq$eq((T) o))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
		System.arraycopy(this.array, 0, store, index, this.count);
	}
	
	@Override
	public Range<T> copy()
	{
		return new ArrayRange(this.array, this.count);
	}
	
	@Override
	public String toString()
	{
		return this.array[0] + " .. " + this.array[this.count - 1];
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Range.rangeEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Range.rangeHashCode(this);
	}
}

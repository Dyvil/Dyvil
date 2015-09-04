package dyvil.collection.range;

import java.util.Iterator;
import java.util.function.Consumer;

import dyvil.lang.literal.NilConvertible;

import dyvil.annotation.object;
import dyvil.collection.Range;
import dyvil.collection.iterator.EmptyIterator;

@NilConvertible
public @object class EmptyRange<T> implements Range<T>
{
	public static final EmptyRange instance = new EmptyRange();
	
	public static <E> EmptyRange<E> apply()
	{
		return instance;
	}
	
	private EmptyRange()
	{
	}
	
	@Override
	public T first()
	{
		return null;
	}
	
	@Override
	public T last()
	{
		return null;
	}
	
	@Override
	public int count()
	{
		return 0;
	}
	
	@Override
	public boolean isHalfOpen()
	{
		return false;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return EmptyIterator.instance;
	}
	
	@Override
	public void forEach(Consumer<? super T> action)
	{
	}
	
	@Override
	public boolean contains(Object o)
	{
		return false;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
	}
	
	@Override
	public Range<T> copy()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "[]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Range)
		{
			return ((Range) obj).estimateCount() == 0;
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return 0;
	}
}

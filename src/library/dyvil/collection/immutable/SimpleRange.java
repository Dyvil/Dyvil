package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import dyvil.lang.Ordered;
import dyvil.lang.Range;
import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
public class SimpleRange<T extends Ordered<T>> implements Range<T>
{
	protected final T	first;
	protected final T	last;
	
	public static <T extends Ordered<T>> SimpleRange<T> apply(T first, T last)
	{
		return new SimpleRange(first, last);
	}
	
	public SimpleRange(T first, T last)
	{
		this.first = first;
		this.last = last;
	}
	
	@Override
	public T first()
	{
		return this.first;
	}
	
	@Override
	public T last()
	{
		return this.last;
	}
	
	@Override
	public int size()
	{
		return -1;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return new Iterator<T>()
		{
			private T	current	= first;
			
			@Override
			public T next()
			{
				if (current.$gt(last))
				{
					throw new NoSuchElementException("End of Range");
				}
				
				T c = current;
				current = current.next();
				return c;
			}
			
			@Override
			public boolean hasNext()
			{
				return current.$lt$eq(last);
			}
		};
	}
}

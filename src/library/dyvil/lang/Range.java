package dyvil.lang;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

import dyvil.collection.immutable.SimpleRange;
import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
public interface Range<T extends Ordered<T>> extends Iterable<T>
{
	public static <T extends Ordered<T>> Range<T> apply(T first, T last)
	{
		return new SimpleRange(first, last);
	}
	
	public T first();
	
	public T last();
	
	public int size();
	
	@Override
	public Iterator<T> iterator();
	
	@Override
	public default Spliterator<T> spliterator()
	{
		int size = this.size();
		int characteristics = Spliterator.ORDERED;
		if (size >= 0)
		{
			characteristics |= Spliterator.SIZED;
		}
		return Spliterators.spliterator(this.iterator(), size, characteristics);
	}
}

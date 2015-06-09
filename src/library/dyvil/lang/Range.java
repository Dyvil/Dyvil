package dyvil.lang;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

import dyvil.collection.range.SimpleRange;
import dyvil.collection.range.StringRange;
import dyvil.lang.literal.TupleConvertible;

@TupleConvertible
public interface Range<T> extends Iterable<T>
{
	public static <T extends Ordered<T>> Range<T> apply(T first, T last)
	{
		return new SimpleRange(first, last);
	}
	
	public static Range<String> apply(String first, String last)
	{
		return new StringRange(first, last);
	}
	
	public T first();
	
	public T last();
	
	public int count();
	
	public default int estimateCount()
	{
		return this.count();
	}
	
	@Override
	public Iterator<T> iterator();
	
	@Override
	public default Spliterator<T> spliterator()
	{
		int size = this.estimateCount();
		int characteristics = Spliterator.ORDERED;
		if (size >= 0)
		{
			characteristics |= Spliterator.SIZED;
		}
		return Spliterators.spliterator(this.iterator(), size, characteristics);
	}
	
	@Override
	public void forEach(Consumer<? super T> action);
	
	public boolean $qmark(Object o);
	
	public default Object[] toArray()
	{
		Object[] array = new Object[this.count()];
		this.toArray(0, array);
		return array;
	}
	
	public default T[] toArray(Class<T> type)
	{
		T[] array = (T[]) Array.newInstance(type, this.count());
		this.toArray(0, array);
		return array;
	}
	
	// toArray
	
	public default void toArray(Object[] store)
	{
		this.toArray(0, store);
	}
	
	public void toArray(int index, Object[] store);
	
	// Copying
	
	public Range<T> copy();
	
	// toString, equals and hashCode
	
	@Override
	public String toString();
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
	public static boolean rangeEquals(Range<?> range, Object o)
	{
		if (!(o instanceof Range))
		{
			return false;
		}
		return rangeEquals(range, (Range) o);
	}
	
	public static boolean rangeEquals(Range<?> range1, Range<?> range2)
	{
		return range1.first().equals(range2.first()) && range1.last().equals(range2.last());
	}
	
	public static int rangeHashCode(Range<?> range)
	{
		return range.first().hashCode() * 31 + range.last().hashCode();
	}
}

package dyvil.collection;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.Covariant;
import dyvil.collection.range.*;
import dyvil.lang.LiteralConvertible;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

@LiteralConvertible.FromNil
@LiteralConvertible.FromTuple
public @Immutable interface Range<@Covariant T> extends Iterable<T>, Serializable
{
	static <T> Range<T> apply()
	{
		return (Range<T>) EmptyRange.instance;
	}

	static IntRange apply(int first, int last)
	{
		return new IntRange(first, last);
	}

	static IntRange halfOpen(int first, int last)
	{
		return new IntRange(first, last, true);
	}

	static LongRange apply(long first, long last)
	{
		return new LongRange(first, last);
	}

	static LongRange halfOpen(long first, long last)
	{
		return new LongRange(first, last, true);
	}

	static FloatRange apply(float first, float last)
	{
		return new FloatRange(first, last);
	}

	static FloatRange halfOpen(float first, float last)
	{
		return new FloatRange(first, last, true);
	}

	static DoubleRange apply(double first, double last)
	{
		return new DoubleRange(first, last);
	}

	static DoubleRange halfOpen(double first, double last)
	{
		return new DoubleRange(first, last, true);
	}
	
	static <T extends Rangeable<T>> Range<T> apply(T first, T last)
	{
		return new ClosedRange<>(first, last);
	}
	
	static <T extends Rangeable<T>> Range<T> halfOpen(T first, T last)
	{
		return new HalfOpenRange<>(first, last);
	}
	
	/**
	 * Returns the first element in this range
	 *
	 * @return the first element in this range
	 */
	T first();
	
	/**
	 * Returns the last element in this range
	 *
	 * @return the last element in this range
	 */
	T last();
	
	/**
	 * Returns the exact number of elements in this range, i.e. the number of elements that would be returned by the
	 * {@link #iterator()}.
	 *
	 * @return the number of elements in this range
	 */
	int count();

	default long longCount()
	{
		return this.count();
	}
	
	/**
	 * Returns an estimate of the number of elements in this range. If the number of elements cannot be directly
	 * computed, {@code -1} should be returned. Otherwise, the result should equal the result of {@link #count()}.
	 *
	 * @return the estimated number of elements in this range
	 */
	default int estimateCount()
	{
		return this.count();
	}
	
	boolean isHalfOpen();
	
	@Override
	Iterator<T> iterator();
	
	@Override
	default Spliterator<T> spliterator()
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
	void forEach(Consumer<? super T> action);
	
	default boolean $qmark(Object o)
	{
		return this.contains(o);
	}
	
	boolean contains(Object o);
	
	// toArray
	
	default Object[] toArray()
	{
		Object[] array = new Object[this.count()];
		this.toArray(0, array);
		return array;
	}
	
	default T[] toArray(Class<T> type)
	{
		T[] array = (T[]) Array.newInstance(type, this.count());
		this.toArray(0, array);
		return array;
	}
	
	default void toArray(Object[] store)
	{
		this.toArray(0, store);
	}
	
	void toArray(int index, Object[] store);
	
	// Copying
	
	Range<T> copy();
	
	// toString, equals and hashCode
	
	@Override
	String toString();
	
	@Override
	boolean equals(Object obj);
	
	@Override
	int hashCode();
	
	static boolean rangeEquals(Range<?> range, Object o)
	{
		return o instanceof Range && rangeEquals(range, (Range) o);
	}
	
	static boolean rangeEquals(Range<?> range1, Range<?> range2)
	{
		return range1.first().equals(range2.first()) && range1.last().equals(range2.last());
	}
	
	static int rangeHashCode(Range<?> range)
	{
		return range.first().hashCode() * 31 + range.last().hashCode();
	}
}

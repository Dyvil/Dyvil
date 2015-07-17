package dyvil.collection;

import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * A <b>BidiQueryable</b> is a specialization of {@link Queryable} adds directed
 * specializations for direction-dependent methods such as
 * {@link #fold(Object, BiFunction)} and {@link #reduce(BiFunction)}. These
 * specializations include {@link #reverseIterator()},
 * {@link #foldLeft(Object, BiFunction)}, {@link #foldRight(Object, BiFunction)}
 * , {@link #reduceLeft(BiFunction)} and {@link #reduceRight(BiFunction)}.
 * 
 * @param <E>
 *            the element type
 */
public interface BidiQueryable<E> extends Queryable<E>
{
	/**
	 * Creates and returns an {@link Iterator} over the elements of this query,
	 * iterating from left to right (first to last element).
	 * 
	 * @return an iterator over the elements of this query
	 */
	@Override
	public Iterator<E> iterator();
	
	/**
	 * Creates and returns an {@link Iterator} over the elements of this query,
	 * iterating from right to left (last to first element).
	 * 
	 * @return a reverse iterator over the elements of this query
	 */
	public Iterator<E> reverseIterator();
	
	@Override
	public default <R> R fold(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return this.foldLeft(initialValue, reducer);
	}
	
	@Override
	public default E reduce(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.reduceLeft(reducer);
	}
	
	public default <R> R foldLeft(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		Iterator<E> iterator = this.iterator();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	public default <R> R foldRight(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		Iterator<E> iterator = this.reverseIterator();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	public default E reduceLeft(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.isEmpty())
		{
			return null;
		}
		
		Iterator<E> iterator = this.iterator();
		E initialValue = iterator.next();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	public default E reduceRight(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.isEmpty())
		{
			return null;
		}
		
		Iterator<E> iterator = this.reverseIterator();
		E initialValue = iterator.next();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
}

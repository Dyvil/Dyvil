package dyvil.collection;

import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * A <b>BidiQueryable</b> is a specialization of {@link Queryable} that adds
 * directed specializations for direction-dependent methods such as {@link
 * #fold(Object, BiFunction)} and {@link #reduce(BiFunction)}. These
 * specializations include {@link #reverseIterator()}, {@link #foldLeft(Object,
 * BiFunction)}, {@link #foldRight(Object, BiFunction)} , {@link
 * #reduceLeft(BiFunction)} and {@link #reduceRight(BiFunction)}.
 *
 * @param <E>
 * 		the element type
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
	Iterator<E> iterator();
	
	/**
	 * Creates and returns an {@link Iterator} over the elements of this query,
	 * iterating from right to left (last to first element).
	 *
	 * @return a reverse iterator over the elements of this query
	 */
	Iterator<E> reverseIterator();

	default E findFirst(Predicate<? super E> condition)
	{
		return this.find(condition);
	}

	default Option<E> findFirstOption(Predicate<? super E> condition)
	{
		return this.findOption(condition);
	}

	default E findLast(Predicate<? super E> condition)
	{
		for (Iterator<E> iterator = this.reverseIterator(); iterator
				.hasNext(); )
		{
			E element = iterator.next();
			if (condition.test(element))
			{
				return element;
			}
		}
		return null;
	}

	default Option<E> findLastOption(Predicate<? super E> condition)
	{
		for (Iterator<E> iterator = this.reverseIterator(); iterator
				.hasNext(); )
		{
			E element = iterator.next();
			if (condition.test(element))
			{
				return new Some<E>(element);
			}
		}
		return None.instance;
	}
	
	@Override
	default <R> R fold(R initialValue,
			BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return this.foldLeft(initialValue, reducer);
	}
	
	@Override
	default E reduce(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return this.reduceLeft(reducer);
	}
	
	default <R> R foldLeft(R initialValue,
			BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		Iterator<E> iterator = this.iterator();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	default <R> R foldRight(R initialValue,
			BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		Iterator<E> iterator = this.reverseIterator();
		while (iterator.hasNext())
		{
			initialValue = reducer.apply(initialValue, iterator.next());
		}
		return initialValue;
	}
	
	default E reduceLeft(BiFunction<? super E, ? super E, ? extends E> reducer)
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
	
	default E reduceRight(BiFunction<? super E, ? super E, ? extends E> reducer)
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

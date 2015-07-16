package dyvil.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Queryable<T> extends Iterable<T>
{
	/**
	 * Returns the number of elements in this query.
	 * 
	 * @return the number of elements
	 */
	public int size();
	
	/**
	 * Returns true iff this query is empty, i.e. the number of elements as
	 * returned by {@link #size()} is {@code 0}.
	 * 
	 * @return true, iff this query is empty
	 */
	public default boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	@Override
	public Iterator<T> iterator();
	
	@Override
	public default Spliterator<T> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	public default Stream<T> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	public default Stream<T> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}
	
	@Override
	public default void forEach(Consumer<? super T> action)
	{
		for (T element : this)
		{
			action.accept(element);
		}
	}
	
	public default <R> R fold(R initialValue, BiFunction<? super R, ? super T, ? extends R> reducer)
	{
		for (T element : this)
		{
			initialValue = reducer.apply(initialValue, element);
		}
		return initialValue;
	}
	
	/*
	 * When converting the Collection API to Dyvil, make sure this method has a
	 * lower-bounded type variable 'R' that is used as the return type. Java
	 * doesn't support this, so we had to introduce a limitation with E.
	 */
	public default T reduce(BiFunction<? super T, ? super T, ? extends T> reducer)
	{
		if (this.isEmpty())
		{
			return null;
		}
		
		Iterator<T> iterator = this.iterator();
		T first = iterator.next();
		while (iterator.hasNext())
		{
			first = reducer.apply(first, iterator.next());
		}
		
		return first;
	}
	
	public default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}
	
	public void map(Function<? super T, ? extends T> mapper);
	
	public void flatMap(Function<? super T, ? extends Iterable<? extends T>> mapper);
	
	public void filter(Predicate<? super T> condition);
}

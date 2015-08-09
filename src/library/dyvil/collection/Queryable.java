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

/**
 * A <b>Queryable</b> represents a special {@link Iterable} that includes a
 * number of special operations for querying. These operations include
 * {@link #stream()} for lazy evaluation, {@link #fold(Object, BiFunction)},
 * {@link #reduce(BiFunction)}, {@link #map(Function)},
 * {@link #flatMap(Function)} and {@link #filter(Predicate)}.
 * 
 * @param <E>
 *            the element type
 */
public interface Queryable<E> extends Iterable<E>
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
	
	/**
	 * Creates and returns an {@link Iterator} over the elements of this query.
	 * 
	 * @return an iterator over the elements of this query
	 */
	@Override
	public Iterator<E> iterator();
	
	/**
	 * Creates and returns a {@link Spliterator} over the elements of this
	 * query.
	 * 
	 * @return a spliterator over the elements of this query
	 */
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), 0);
	}
	
	/**
	 * Creates and returns a sequential {@link Stream} of this query, based on
	 * the {@link Spliterator} returned by {@link #spliterator()}.
	 * 
	 * @return a stream of this query
	 */
	public default Stream<E> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	/**
	 * Creates and returns a parallel {@link Stream} of this query, based on the
	 * {@link Spliterator} returned by {@link #spliterator()}.
	 * 
	 * @return a parallel stream of this query
	 */
	public default Stream<E> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}
	
	@Override
	public default void forEach(Consumer<? super E> action)
	{
		for (E element : this)
		{
			action.accept(element);
		}
	}
	
	/**
	 * Folds this entire query into a single value by repeatedly reducing the
	 * elements of this query and the initial value using the given
	 * {@code reducer}. If this query does not contain any elements, the initial
	 * value is simply returned. Otherwise, it is repeatedly replaced with the
	 * result of {@code reducer.apply(initialValue, element)} for every element
	 * in this query.
	 * 
	 * @param initialValue
	 *            the initial value
	 * @param reducer
	 *            the reducer function
	 * @return the folded value
	 */
	public default <R> R fold(R initialValue, BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		for (E element : this)
		{
			initialValue = reducer.apply(initialValue, element);
		}
		return initialValue;
	}
	
	/**
	 * Reduces this entire query into a single value by repeatedly reducing the
	 * elements using the given {@code reducer} binary operator.
	 * 
	 * @param reducer
	 *            the reducer binary operator
	 * @return the reduced value
	 */
	/*
	 * When converting the Collection API to Dyvil, make sure this method has a
	 * lower-bounded type variable 'R' that is used as the return type. Java
	 * doesn't support this, so we had to introduce a limitation with E.
	 */
	public default E reduce(BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		if (this.isEmpty())
		{
			return null;
		}
		
		Iterator<E> iterator = this.iterator();
		E first = iterator.next();
		while (iterator.hasNext())
		{
			first = reducer.apply(first, iterator.next());
		}
		
		return first;
	}
	
	/**
	 * Returns true if and if only this query contains the given {@code element}
	 * . By default, 'contains' in defined such that any element in this query
	 * matches the given element in a way so that
	 * {@code element.equals(this.element)}.
	 * 
	 * @param element
	 *            the element to find
	 * @return true, iff this query contains the element
	 */
	public default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}
	
	/**
	 * Maps all elements in this query using the given {@code mapper} function
	 * by supplying them to the function and replacing them with the result of
	 * the call {mapper.apply(element)}.
	 * 
	 * @param mapper
	 *            the mapping function
	 */
	public void map(Function<? super E, ? extends E> mapper);
	
	/**
	 * Maps all elements in this query using the given {@code mapper} function
	 * by supplying them to the function and replacing them with all results of
	 * the call {@code mapper.apply(element)}. If the mapper returns multiple
	 * results at once, they are 'flattened' and added to this query
	 * sequentially as shown in the below example.
	 * 
	 * <pre>
	 * List[int] list = MutableList(1, 2, 3, 4)
	 * list.flatMap(i => [ i, i * 10 ])
	 * println list // prints [ 1, 10, 2, 20, 3, 30, 4, 40 ]
	 * </pre>
	 * 
	 * @param mapper
	 *            the mapping function
	 */
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	/**
	 * Removes all elements from the query that do not fulfill the requirement
	 * given by the {@code condition}, i.e. if {@code condition.test(element)}
	 * returns {@code false}.
	 * 
	 * @param condition
	 *            the condition
	 */
	public void filter(Predicate<? super E> condition);
	
	public default String toString(String prefix, String separator, String postfix)
	{
		StringBuilder builder = new StringBuilder();
		this.toString(builder, prefix, separator, postfix);
		return builder.toString();
	}
	
	public default void toString(StringBuilder builder, String prefix, String separator, String postfix)
	{
		builder.append(prefix);
		if (this.isEmpty())
		{
			builder.append(postfix);
			return;
		}
		
		Iterator<E> iterator = this.iterator();
		E first = iterator.next();
		builder.append(first);
		while (iterator.hasNext())
		{
			builder.append(separator);
			builder.append(iterator.next());
		}
		builder.append(postfix);
	}
}

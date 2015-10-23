package dyvil.collection;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A <b>Queue</b> represents a FIFO (<b>F</b>irst <b>I</b>n, <b>F</b>irst
 * <b>O</b>ut) mutable data structure. That means that elements will be
 * processed in the order in which they were added to the queue, so that the
 * first element that was added will be the first element to be removed, and
 * vice-versa.
 * 
 * @see Stack
 * @see Deque
 * @author Clashsoft
 * @param <E>
 *            the element type of the queue
 */
public interface Queue<E> extends Queryable<E>, Serializable
{
	/**
	 * Returns the number of elements in this queue.
	 * 
	 * @return the number of elements in this queue.
	 */
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	/**
	 * Returns true if and if only this queue contains the given {@code element}
	 * .
	 * 
	 * @param element
	 *            the element to find
	 * @return true iff this queue contains the element
	 */
	@Override
	public default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}
	
	/**
	 * Removes all elements from this queue.
	 */
	public void clear();
	
	/**
	 * Appends the given element to the end of this queue.
	 * 
	 * @param element
	 *            the element to append
	 */
	public void offer(E element);
	
	/**
	 * Appends all elements in the given collection of {@code elements} to the
	 * end of this queue in the order in which they appear in the collection.
	 * 
	 * @param elements
	 *            the elements to append
	 */
	public default void offerAll(Iterable<? extends E> elements)
	{
		for (E e : elements)
		{
			this.offer(e);
		}
	}
	
	/**
	 * Removes and returns the last element in this queue. If this queue is
	 * empty, {@code null} is returned.
	 * 
	 * @return the last element in this queue.
	 */
	public E remove();
	
	/**
	 * Removes the given number of elements from the end of this queue, as if by
	 * calling {@link #remove()} {@code count} times.
	 * 
	 * @param count
	 *            the number of elements to remove
	 */
	public default void remove(int count)
	{
		for (int i = 0; i < count; i++)
		{
			this.remove();
		}
	}
	
	/**
	 * Returns the last element in this queue. Unlike {@link #remove()}, this
	 * method does not remove the element from the end.
	 * 
	 * @return the last element in this queue.
	 */
	public E element();
	
	@Override
	public void map(Function<? super E, ? extends E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	/**
	 * Returns a copy of this queue that contains the same elements as this
	 * queue in the same order.
	 * 
	 * @return a copy of this queue.
	 */
	public Queue<E> copy();
}

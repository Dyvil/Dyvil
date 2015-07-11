package dyvil.collection;

import java.util.Iterator;

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
public interface Queue<E> extends Iterable<E>
{
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	public default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}
	
	public void clear();
	
	public void offer(E element);
	
	public default void offerAll(Iterable<? extends E> elements)
	{
		for (E e : elements)
		{
			this.offer(e);
		}
	}
	
	public E remove();
	
	public default void remove(int count)
	{
		for (int i = 0; i < count; i++)
		{
			this.remove(count);
		}
	}
	
	public E element();
	
	public Queue<E> copy();
}

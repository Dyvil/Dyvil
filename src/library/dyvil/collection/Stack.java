package dyvil.collection;

import java.util.Iterator;

import dyvil.lang.Collection;

/**
 * A <b>Stack</b> represents a LIFO (<b>L</b>ast <b>I</b>n, <b>F</b>irst
 * <b>O</b>ut) mutable data structure. That means that elements will be
 * processed in the reverse order in which they were added to the queue, so that
 * the first element that was added will be the last element to be removed, and
 * vice-versa.
 * 
 * @see Queue
 * @see Deque
 * @author Clashsoft
 * @param <E>
 *            the element type of the stack
 */
public interface Stack<E> extends Iterable<E>
{
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	public default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}
	
	public void clear();
	
	public void push(E e);
	
	public default void pushAll(Iterable<? extends E> elements)
	{
		for (E e : elements)
		{
			this.push(e);
		}
	}
	
	public E pop();
	
	public default void pop(int count)
	{
		for (int i = 0; i < count; i++)
		{
			this.pop();
		}
	}
	
	public E peek();
	
	public Stack<E> copy();
}

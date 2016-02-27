package dyvil.collection;

import java.io.Serializable;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A <b>Stack</b> represents a LIFO (<b>L</b>ast <b>I</b>n, <b>F</b>irst <b>O</b>ut) mutable data structure. That means
 * that elements will be processed in the reverse order in which they were added to the queue, so that the first element
 * that was added will be the last element to be removed, and vice-versa.
 *
 * @param <E>
 * 		the element type of the stack
 *
 * @author Clashsoft
 * @see Queue
 * @see Deque
 */
public interface Stack<E> extends Queryable<E>, Serializable
{
	/**
	 * Returns the number of elements in this stack.
	 *
	 * @return the number of elements in this Stack.
	 */
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	/**
	 * Returns true if and if only this stack contains the given {@code element} .
	 *
	 * @param element
	 * 		the element to find
	 *
	 * @return true iff this stack contains the element
	 */
	@Override
	default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}
	
	/**
	 * Removes all elements from this stack.
	 */
	void clear();
	
	/**
	 * Adds the given element to the top of this stack, so that it becomes the first element in the stack.
	 *
	 * @param element
	 * 		the element to add
	 */
	void push(E e);
	
	/**
	 * Adds all elements in the given collection of {@code elements} to the top of this stack in the order in which they
	 * appear in the collection.
	 *
	 * @param elements
	 * 		the elements to add
	 */
	default void pushAll(Iterable<? extends E> elements)
	{
		for (E e : elements)
		{
			this.push(e);
		}
	}
	
	/**
	 * Removes and returns the first element from the top of this stack. If this stack is empty, {@code null} is
	 * returned.
	 *
	 * @return the top element of this stack.
	 */
	E pop();
	
	/**
	 * Removes the given number of elements from the top of this stack, as if by calling {@link #pop()} {@code count}
	 * times.
	 *
	 * @param count
	 * 		the number of elements to remove
	 */
	default void pop(int count)
	{
		for (int i = 0; i < count; i++)
		{
			this.pop();
		}
	}
	
	/**
	 * Returns the top element of this stack. Unlike {@link #pop()}, this method does not remove the element from the
	 * top.
	 *
	 * @return the top element of this stack.
	 */
	E peek();
	
	@Override
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	void filter(Predicate<? super E> condition);
	
	/**
	 * Returns a copy of this stack that contains the same elements as this stack in the same order.
	 *
	 * @return a copy of this stack.
	 */
	Stack<E> copy();
}

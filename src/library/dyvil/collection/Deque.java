package dyvil.collection;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A <b>Deque</b> (<b>D</b>ouble-<b>e</b>nded <b>Que</b>ue) is a mutable data
 * structure that supports insertion and removal of elements at both ends,
 * therefore representing both a {@link Stack} and a {@link Queue}.
 *
 * @param <E>
 * 		the element type
 */
public interface Deque<E> extends BidiQueryable<E>, Queue<E>, Stack<E>
{
	// Collection Methods
	
	@Override
	default boolean contains(Object o)
	{
		return Collection.iterableContains(this, o);
	}
	
	/**
	 * Returns the number of elements in this deque.
	 *
	 * @return ths number of elements in this deque.
	 */
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	@Override
	Iterator<E> reverseIterator();
	
	// Deque Methods
	
	/**
	 * Removes all elements form this deque.
	 */
	@Override
	void clear();
	
	/**
	 * Adds the given element to the front of this deque.
	 *
	 * @param e
	 * 		the element to add.
	 */
	void addFirst(E e);
	
	/**
	 * Adds the given element to the end of this deque.
	 *
	 * @param e
	 * 		the element to add.
	 */
	void addLast(E e);
	
	/**
	 * Removes and returns the first element in this deque.
	 *
	 * @return the first element in this deque.
	 */
	E removeFirst();
	
	/**
	 * Removes and returns the last element in this deque.
	 *
	 * @return the last element in this deque.
	 */
	E removeLast();
	
	/**
	 * Returns, but does not remove, the first element in this deque.
	 *
	 * @return the first element in this deque.
	 */
	E getFirst();
	
	/**
	 * Returns, but does not remove, the last element in this deque.
	 *
	 * @return the last element in this deque.
	 */
	E getLast();
	
	/**
	 * Removes the first occurence of the given {@code element} from this deque
	 * and returns {@code true} if the element was removed. If the element could
	 * not be removed or was not found, {@code false} is returned.
	 *
	 * @param element
	 * 		the element to remove
	 *
	 * @return true, if the element was removed successfully
	 */
	boolean removeFirst(Object o);
	
	/**
	 * Removes the last occurence of the given {@code element} from this deque
	 * and returns {@code true} if the element was removed. If the element could
	 * not be removed or was not found, {@code false} is returned.
	 *
	 * @param element
	 * 		the element to remove
	 *
	 * @return true, if the element was removed successfully
	 */
	boolean removeLast(Object o);
	
	@Override
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void filter(Predicate<? super E> condition);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	// Queue Methods
	
	@Override
	default void offer(E e)
	{
		this.addLast(e);
	}
	
	@Override
	default E remove()
	{
		return this.removeLast();
	}
	
	@Override
	default E element()
	{
		return this.getLast();
	}
	
	// Stack Methods
	
	@Override
	default void push(E e)
	{
		this.addFirst(e);
	}
	
	@Override
	default E pop()
	{
		return this.removeFirst();
	}
	
	@Override
	default E peek()
	{
		return this.getFirst();
	}
	
	/**
	 * Returns a copy of this deque that contains the same elements as this
	 * deque in the same order.
	 *
	 * @return a copy of this deque.
	 */
	@Override
	Deque<E> copy();
}

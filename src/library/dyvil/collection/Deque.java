package dyvil.collection;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A <b>Deque</b> (<b>D</b>ouble-<b>e</b>nded <b>Que</b>ue) is a mutable data structure that supports insertion and
 * removal of elements at both ends, therefore representing both a {@link Stack} and a {@link Queue}.
 *
 * @param <E>
 * 	the element type
 */
public interface Deque<E> extends BidiQueryable<E>, Queue<E>, Stack<E>
{
	// Collection Methods

	@Override
	default boolean contains(Object element)
	{
		return Collection.iterableContains(this, element);
	}

	/**
	 * Returns the number of elements in this deque.
	 *
	 * @return ths number of elements in this deque.
	 */
	@Override
	int size();

	@NonNull
	@Override
	Iterator<E> iterator();

	@NonNull
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
	 * @param element
	 * 	the element to add.
	 */
	void addFirst(E element);

	/**
	 * Adds the given element to the end of this deque.
	 *
	 * @param element
	 * 	the element to add.
	 */
	void addLast(E element);

	/**
	 * Removes and returns the first element in this deque.
	 *
	 * @return the first element in this deque.
	 */
	@Nullable E removeFirst();

	/**
	 * Removes and returns the last element in this deque.
	 *
	 * @return the last element in this deque.
	 */
	@Nullable E removeLast();

	/**
	 * Returns, but does not remove, the first element in this deque.
	 *
	 * @return the first element in this deque.
	 */
	@Nullable E getFirst();

	/**
	 * Returns, but does not remove, the last element in this deque.
	 *
	 * @return the last element in this deque.
	 */
	@Nullable E getLast();

	/**
	 * Removes the first occurence of the given {@code element} from this deque and returns {@code true} if the element
	 * was removed. If the element could not be removed or was not found, {@code false} is returned.
	 *
	 * @param element
	 * 	the element to remove
	 *
	 * @return true, if the element was removed successfully
	 */
	boolean removeFirst(Object element);

	/**
	 * Removes the last occurence of the given {@code element} from this deque and returns {@code true} if the element
	 * was removed. If the element could not be removed or was not found, {@code false} is returned.
	 *
	 * @param element
	 * 	the element to remove
	 *
	 * @return true, if the element was removed successfully
	 */
	boolean removeLast(Object element);

	@Override
	void map(@NonNull Function<? super E, ? extends E> mapper);

	@Override
	void filter(@NonNull Predicate<? super E> condition);

	@Override
	void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper);

	// Queue Methods

	@Override
	default void offer(E element)
	{
		this.addLast(element);
	}

	@Nullable
	@Override
	default E remove()
	{
		return this.removeLast();
	}

	@Nullable
	@Override
	default E element()
	{
		return this.getLast();
	}

	// Stack Methods

	@Override
	default void push(E element)
	{
		this.addFirst(element);
	}

	@Nullable
	@Override
	default E pop()
	{
		return this.removeFirst();
	}

	@Nullable
	@Override
	default E peek()
	{
		return this.getFirst();
	}

	@Nullable
	@Override
	E peek(int index);

	/**
	 * Returns a copy of this deque that contains the same elements as this deque in the same order.
	 *
	 * @return a copy of this deque.
	 */
	@NonNull
	@Override
	Deque<E> copy();
}

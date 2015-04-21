package dyvil.lang;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.immutable.ImmutableList;
import dyvil.collection.mutable.MutableList;
import dyvil.lang.literal.ArrayConvertible;

public interface List<E> extends Collection<E>, ArrayConvertible
{
	public static <E> MutableList<E> apply()
	{
		return new dyvil.collection.mutable.ArrayList();
	}
	
	public static <E> ImmutableList<E> apply(E[] array)
	{
		return new dyvil.collection.immutable.ArrayList(array);
	}
	
	// Simple getters
	
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public Spliterator<E> spliterator();
	
	@Override
	public void forEach(Consumer<? super E> action);
	
	@Override
	public boolean $qmark(Object element);
	
	/**
	 * Returns the element at the given {@code index}. This method throws an
	 * {@link IndexOutOfBoundsException} if the given {@code index} is less than
	 * {@code 0} or greater than or equal to the size of this list.
	 * 
	 * @param index
	 *            the index
	 * @return the element at the given index
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the bounds of this list
	 */
	public E apply(int index);
	
	/**
	 * Returns the element at the given {@code index}. Unlike
	 * {@link #apply(int)}, this method will not throw any exceptions if the
	 * given {@code index} is out of bounds. Instead, it silently ignores the
	 * error and returns {@code null}.
	 * 
	 * @param index
	 *            the index
	 * @return the element at the given index
	 */
	public E get(int index);
	
	// Non-mutating Operations
	
	public List<E> slice(int startIndex, int length);
	
	@Override
	public List<E> $plus(E element);
	
	@Override
	public List<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public List<E> $minus(E element);
	
	@Override
	public List<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public List<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> List<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> List<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public List<E> filtered(Predicate<? super E> condition);
	
	@Override
	public List<E> sorted();
	
	@Override
	public List<E> sorted(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	/**
	 * Resizes this list to the given size. This method, unlike
	 * {@link #ensureCapacity(int)}, actually changes the size as returned by
	 * {@link #size()}. If the {@code newSize} is greater than the current size
	 * of this list, this method should add {@code null} to the list until the
	 * new size is reached. If the {@code newSize} is smaller than the current
	 * size, it should remove elements from the end of the list until the new
	 * size is reached.
	 * 
	 * @param newSize
	 *            the new size
	 */
	public void resize(int newSize);
	
	/**
	 * Ensures the capacity of this list to be at least {@code minSize}. This
	 * can be used to avoid having to recreate arrays in {@link ArrayList}s when
	 * the amount of elements to be added is already known.
	 * 
	 * @param minSize
	 *            the minimum size
	 */
	public default void ensureCapacity(int minSize)
	{
	}
	
	/**
	 * Updates the element at the given {@code index} of this list. This method
	 * throws an {@link IndexOutOfBoundsException} if the given {@code index} is
	 * less than {@code 0} or greater than or equal to the size of this list.
	 * 
	 * @param index
	 *            the index of the element to be updated
	 * @param element
	 *            the new element
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the bounds of this list
	 */
	public void update(int index, E element);
	
	/**
	 * Updates the element at the given {@code index} of this list. Unlike
	 * {@link #update(int, Object)}, this method will not throw any exceptions
	 * if the given {@code index} is out of bounds. Instead, it silently ignores
	 * the error and returns {@code null}.
	 * 
	 * @param index
	 *            the index of the element to be updated
	 * @param element
	 *            the new element
	 * @return the old element, if present, {@code null} otherwise
	 */
	public E set(int index, E element);
	
	/**
	 * Inserts the element at the given {@code index} of this list. This method
	 * throws an {@link IndexOutOfBoundsException} if the given {@code index} is
	 * less than {@code 0} or greater than the size of this list.
	 * 
	 * @param index
	 *            the index at which to insert the element
	 * @param element
	 *            the element to be inserted
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of the bounds of this list
	 */
	public void insert(int index, E element);
	
	@Override
	public E add(E element);
	
	/**
	 * Inserts the element at the given {@code index} of this list. Unlike
	 * {@link #insert(int, Object)}, this method will not throw any exception if
	 * the given {@code index} is out of bounds. Instead, it simply resizes this
	 * list to it's needs and returns {@code null}.
	 * 
	 * @param index
	 *            the index at which to insert the element
	 * @param element
	 *            the element to be inserted
	 * @return the old element, if present, {@code null} otherwise
	 */
	public E add(int index, E element);
	
	@Override
	public boolean remove(E element);
	
	/**
	 * Removes the element at the given {@code index} from this list. This
	 * method throws an {@link IndexOutOfBoundsException} if the given
	 * {@code index} is less than {@code 0} or greater than or equal to the size
	 * of this list.
	 * 
	 * @param index
	 *            the index of the element to remove from this list
	 */
	public void removeAt(int index);
	
	@Override
	public void $plus$eq(E element);
	
	@Override
	public void $plus$plus$eq(Collection<? extends E> collection);
	
	@Override
	public void $minus$eq(E element);
	
	@Override
	public void $minus$minus$eq(Collection<? extends E> collection);
	
	@Override
	public void $amp$eq(Collection<? extends E> collection);
	
	@Override
	public void clear();
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	@Override
	public void map(UnaryOperator<E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	public void sort();
	
	@Override
	public void sort(Comparator<? super E> comparator);
	
	// Search Operations
	
	/**
	 * Returns the first index of the given {@code element} in this list, and
	 * {@code -1} if the element is not present in this list.
	 * 
	 * @param element
	 *            the element to search
	 * @return the first index of the element
	 */
	public int indexOf(E element);
	
	/**
	 * Returns the last index of the given {@code element} in this list, and
	 * {@code -1} if the element is not present in this list.
	 * 
	 * @param element
	 *            the element to search
	 * @return the last index of the element
	 */
	public int lastIndexOf(E element);
	
	// Copying
	
	@Override
	public List<E> copy();
	
	@Override
	public MutableList<E> mutable();
	
	@Override
	public ImmutableList<E> immutable();
}

package dyvil.lang;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.ImmutableList;
import dyvil.collection.MutableList;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

/**
 * A <b>List</b> is a data type that represents an ordered (sequential)
 * {@linkplain Collection collection}. A list supports access using integer
 * indexes in a way similar to arrays. However, they also support various
 * operations for easily modifying the structure of their elements, examples for
 * which are the {@linkplain #add(Object) add} or {@linkplain #remove(Object)
 * remove} operations. Since a list is also a {@linkplain Collection}, it also
 * supports various querying operations including {@linkplain #map(Function)
 * map}, {@linkplain #filter(Predicate) filter} or
 * {@linkplain #flatMap(Function) flatMap} and new sequential operations such as
 * {@linkplain #sort() sort}.
 * <p>
 * As with {@linkplain Collection collections}, lists also make a clear
 * distinction between {@linkplain MutableList mutable} and
 * {@linkplain ImmutableList immutable} data. For the latter, the <i>Dyvil
 * Collection Framework</i> provides various memory-efficient implementations
 * specialized for lists with zero, one or multiple elements.
 * <p>
 * Since this interface is both {@link NilConvertible} and
 * {@link ArrayConvertible}, it is possible to initialize both mutable and
 * immutable lists with simple expressions, as shown in the below example.
 * 
 * <pre>
 * List[int] mutable = nil // Creates an empty, mutable list
 * List[String] immutable = [ "a", "b", "c" ] // Creates an immutable list from the array
 * </pre>
 * 
 * @author Clashsoft
 * @param <E>
 *            the element type
 */
@NilConvertible
@ArrayConvertible
public interface List<E> extends Collection<E>
{
	/**
	 * Returns an empty, mutable list. This method is primarily for use with the
	 * {@code nil} literal in <i>Dyvil</i> and internally creates an empty
	 * {@link dyvil.collection.mutable.ArrayList ArrayList}.
	 * 
	 * @return an empty, mutable list
	 */
	public static <E> MutableList<E> apply()
	{
		return new dyvil.collection.mutable.ArrayList();
	}
	
	/**
	 * Returns an immutable list containing all of the given {@code elements}.
	 * This method is primarily for use with <i>Array Expressions</i> in
	 * <i>Dyvil</i> and internally creates an
	 * {@link dyvil.collection.immutable.ArrayList ArrayList} from the given
	 * {@code elements}.
	 * 
	 * @param elements
	 *            the elements of the returned collection
	 * @return an immutable list containing all of the given elements
	 */
	public static <E> ImmutableList<E> apply(E... array)
	{
		return new dyvil.collection.immutable.ArrayList(array, true);
	}
	
	// Simple getters
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.SIZED);
	}
	
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
	
	/**
	 * Creates and returns a {@linkplain List} containing {@code length} of the
	 * elements of this list starting from the {@code startIndex}. If the
	 * {@code startIndex} or the end index ({@code startIndex + length}) exceeds
	 * the size of this list, an exception will be thrown.
	 * <p>
	 * Note that for {@linkplain MutableList mutable lists}, it is not
	 * guaranteed that changes to the sub-list will be reflected in the list is
	 * was created from. Although it is not an absolute requirement,
	 * implementations should return a list that, when mutated, does <b>not</b>
	 * reflect the changes in this list. This behavior is implemented in all
	 * {@linkplain MutableList mutable list} implementations of the <i>Dyvil
	 * Collection Framework</i>.
	 * 
	 * @param startIndex
	 *            the start index of the sub list
	 * @param length
	 *            the length of the sub list
	 * @return a sub-list with {@code length} elements starting from the
	 *         {@code startIndex}
	 */
	public List<E> subList(int startIndex, int length);
	
	@Override
	public List<E> $plus(E element);
	
	@Override
	public List<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public List<E> $minus(Object element);
	
	@Override
	public List<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public List<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public List<? extends E> $bar(Collection<? extends E> collection);
	
	@Override
	public List<? extends E> $up(Collection<? extends E> collection);
	
	@Override
	public <R> List<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> List<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public List<E> filtered(Predicate<? super E> condition);
	
	/**
	 * Returns a list that contains the same elements as this list, but in a
	 * sorted order. The sorting order is given by the <i>natural order</i> of
	 * the elements of this list, i.e. the order specified by their
	 * {@link Comparable#compareTo(Object) compareTo} method. Thus, this method
	 * will fail if the elements of this list do not implement
	 * {@link Comparable} interface.
	 * 
	 * @return a sorted list of this list's elements
	 */
	public List<E> sorted();
	
	/**
	 * Returns a list that contains the same elements as this list, but in a
	 * sorted order. The sorting order is specified by the given
	 * {@code comparator}.
	 * 
	 * @param comparator
	 *            the comparator that defines the order of the elements
	 * @return a sorted list of this list's elements using the given comparator
	 */
	public List<E> sorted(Comparator<? super E> comparator);
	
	public List<E> distinct();
	
	public List<E> distinct(Comparator<? super E> comparator);
	
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
	public boolean add(E element);
	
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
	public default boolean remove(E element)
	{
		int index = this.indexOf(element);
		if (index == -1)
		{
			return false;
		}
		this.removeAt(index);
		return true;
	}
	
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
	public void $amp$eq(Collection<? extends E> collection);
	
	@Override
	public void $bar$eq(Collection<? extends E> collection);
	
	@Override
	public void $up$eq(Collection<? extends E> collection);
	
	@Override
	public void clear();
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	@Override
	public void map(UnaryOperator<E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	/**
	 * Sorts the elements of this list. The sorting order is given by the
	 * <i>natural order</i> of the elements of this collection, i.e. the order
	 * specified by their {@link Comparable#compareTo(Object) compareTo} method.
	 * Thus, this method will fail if the elements in this collection do not
	 * implement {@link Comparable} interface.
	 */
	public void sort();
	
	/**
	 * Sorts the elements of this list. The sorting order is specified by the
	 * given {@code comparator}.
	 * 
	 * @param comparator
	 *            the comparator that defines the order of the elements
	 */
	public void sort(Comparator<? super E> comparator);
	
	public void distinguish();
	
	public void distinguish(Comparator<? super E> comparator);
	
	// Search Operations
	
	/**
	 * Returns the first index of the given {@code element} in this list, and
	 * {@code -1} if the element is not present in this list.
	 * 
	 * @param element
	 *            the element to search
	 * @return the first index of the element
	 */
	public int indexOf(Object element);
	
	/**
	 * Returns the last index of the given {@code element} in this list, and
	 * {@code -1} if the element is not present in this list.
	 * 
	 * @param element
	 *            the element to search
	 * @return the last index of the element
	 */
	public int lastIndexOf(Object element);
	
	// toArray
	
	@Override
	public void toArray(int index, Object[] store);
	
	// Copying
	
	@Override
	public List<E> copy();
	
	@Override
	public MutableList<E> mutable();
	
	@Override
	public ImmutableList<E> immutable();

	public static boolean listEquals(List<?> list, Object o)
	{
		if (!(o instanceof List))
		{
			return false;
		}
		
		return listEquals((List) list, (List) o);
	}

	public static boolean listEquals(List<?> c1, List<?> c2)
	{
		if (c1.size() != c2.size())
		{
			return false;
		}
		
		Iterator iterator1 = c1.iterator();
		Iterator iterator2 = c2.iterator();
		while (iterator1.hasNext())
		{
			Object o1 = iterator1.next();
			Object o2 = iterator2.next();
			if (!Objects.equals(o1, o2))
			{
				return false;
			}
		}
		return true;
	}

	public static int listHashCode(List<?> list)
	{
		int result = 1;
		for (Object o : list)
		{
			result = 31 * result + (o == null ? 0 : o.hashCode());
		}
		return result;
	}
}

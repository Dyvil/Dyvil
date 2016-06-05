package dyvil.collection;

import dyvil.collection.immutable.EmptyList;
import dyvil.collection.mutable.ArrayList;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.ref.ObjectRef;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * A <b>List</b> is a data type that represents an ordered (sequential) {@linkplain Collection collection}. A list
 * supports access using integer indexes in a way similar to arrays. However, they also support various operations for
 * easily modifying the structure of their elements, examples for which are the {@linkplain #add(Object) add} or
 * {@linkplain #remove(Object) remove} operations. Since a list is also a {@linkplain Collection}, it also supports
 * various querying operations including {@linkplain #map(Function) map}, {@linkplain #filter(Predicate) filter} or
 * {@linkplain #flatMap(Function) flatMap} and new sequential operations such as {@linkplain #sort() sort}.
 * <p>
 * As with {@linkplain Collection collections}, lists also make a clear distinction between {@linkplain MutableList
 * mutable} and {@linkplain ImmutableList immutable} data. For the latter, the <i>Dyvil Collection Framework</i>
 * provides various memory-efficient implementations specialized for lists with zero, one or multiple elements.
 * <p>
 * Since this interface is both {@link NilConvertible} and {@link ArrayConvertible}, it is possible to initialize both
 * mutable and immutable lists with simple expressions, as shown in the below example.
 * <p>
 * <pre>
 * List[int] mutable = nil // Creates an empty, mutable list
 * List[String] immutable = [ "a", "b", "c" ] // Creates an immutable list from the array
 * </pre>
 *
 * @param <E>
 * 		the element type
 *
 * @author Clashsoft
 */
@NilConvertible(methodName = "empty")
@ArrayConvertible
public interface List<E> extends Collection<E>, BidiQueryable<E>
{
	/**
	 * Returns an empty, immutable list. This method is primarily for use with the {@code nil} literal in <i>Dyvil</i>
	 * and returns an instance of {@link EmptyList}.
	 *
	 * @return an empty, immutable list
	 */
	static <E> ImmutableList<E> empty()
	{
		return ImmutableList.apply();
	}
	
	/**
	 * Returns an empty, mutable list. The exact type of the returned object is given by {@link MutableList#apply()}.
	 *
	 * @return an empty, mutable list
	 */
	static <E> MutableList<E> apply()
	{
		return MutableList.apply();
	}
	
	static <E> ImmutableList<E> apply(E element)
	{
		return ImmutableList.apply(element);
	}
	
	/**
	 * Returns an immutable list containing all of the given {@code elements}. This method is primarily for use with
	 * <i>Array Expressions</i> in <i>Dyvil</i> and internally creates an {@link dyvil.collection.immutable.ArrayList
	 * ArrayList} from the given {@code elements}.
	 *
	 * @param elements
	 * 		the elements of the returned collection
	 *
	 * @return an immutable list containing all of the given elements
	 */
	@SafeVarargs
	static <E> ImmutableList<E> apply(E... elements)
	{
		return ImmutableList.apply(elements);
	}
	
	static <E> ImmutableList<E> from(E[] array)
	{
		return ImmutableList.from(array);
	}

	static <E> ImmutableList<E> from(Iterable<? extends E> array)
	{
		return ImmutableList.from(array);
	}

	static <E> ImmutableList<E> from(Collection<? extends E> array)
	{
		return ImmutableList.from(array);
	}

	static <E> ImmutableList<E> repeat(int count, E repeatedValue)
	{
		return ImmutableList.repeat(count, repeatedValue);
	}
	
	static <E> ImmutableList<E> generate(int count, IntFunction<E> generator)
	{
		return ImmutableList.generate(count, generator);
	}
	
	// Simple getters
	
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	@Override
	Iterator<E> reverseIterator();
	
	@Override
	default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.SIZED);
	}
	
	/**
	 * Returns the element at the given {@code index}. By default, this method delegates to the {@link #get(int)}
	 * method.
	 *
	 * @param index
	 * 		the index
	 *
	 * @return the element at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 * 		if the index is out of the bounds of this list
	 */
	default E subscript(int index)
	{
		return this.get(index);
	}

	default List<E> subscript(Range<Integer> range)
	{
		int start = range.first();
		int length = range.count();
		return this.subList(start, length);
	}

	/**
	 * Returns a reference that points to the value at the given {@code index}. The reference delegates to the {@link
	 * #get(int)} and {@link #setResizing(int, Object)} methods, meaning that it behaves just like these methods.
	 *
	 * @param index
	 * 		the index the reference points at
	 *
	 * @return a reference that points to the value at the given index
	 */
	default ObjectRef<E> subscript_$amp(int index)
	{
		return new ObjectRef<E>()
		{
			@Override
			public E get()
			{
				return List.this.get(index);
			}

			@Override
			public void set(E value)
			{
				List.this.setResizing(index, value);
			}
		};
	}

	/**
	 * Returns the element at the given {@code index}. This method throws an {@link IndexOutOfBoundsException} if the
	 * given {@code index} is less than {@code 0} or greater than or equal to the size of this list.
	 *
	 * @param index
	 * 		the index
	 *
	 * @return the element at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 * 		if the index is out of the bounds of this list
	 */
	E get(int index);
	
	// Non-mutating Operations
	
	/**
	 * Creates and returns a {@linkplain List} containing {@code length} of the elements of this list starting from the
	 * {@code startIndex}. If the {@code startIndex} or the end index ({@code startIndex + length}) exceeds the size of
	 * this list, an exception will be thrown.
	 * <p>
	 * For mutable lists, this method returns a list that is not connected to this one. Changes to either this list or
	 * the result are not reflected in the other list.
	 *
	 * @param startIndex
	 * 		the start index of the sub list
	 * @param length
	 * 		the length of the sub list
	 *
	 * @return a list with {@code length} elements starting from the {@code startIndex}
	 */
	List<E> subList(int startIndex, int length);

	@Override
	List<E> added(E element);
	
	@Override
	List<? extends E> union(Collection<? extends E> collection);
	
	/**
	 * {@inheritDoc} Since {@link List Lists} can contain that same element multiple times, implementations should
	 * behave so that <i>all</i> occurrences of the element are removed, not only the first one. This behavior can be
	 * achieved using this code snippet:
	 * <p>
	 * <pre>
	 * List copy = list.copy()
	 * copy.removeAt(list.indexOf(element))
	 * </pre>
	 */
	@Override
	List<E> removed(Object element);
	
	@Override
	List<? extends E> difference(Collection<?> collection);
	
	@Override
	List<? extends E> intersection(Collection<? extends E> collection);
	
	@Override
	<R> List<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	<R> List<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	List<E> filtered(Predicate<? super E> condition);
	
	List<E> reversed();
	
	/**
	 * Returns a list that contains the same elements as this list, but in a sorted order. The sorting order is given by
	 * the <i>natural order</i> of the elements of this list, i.e. the order specified by their {@link
	 * Comparable#compareTo(Object) compareTo} method. Thus, this method will fail if the elements of this list do not
	 * implement {@link Comparable} interface.
	 *
	 * @return a sorted list of this list's elements
	 */
	List<E> sorted();
	
	/**
	 * Returns a list that contains the same elements as this list, but in a sorted order. The sorting order is
	 * specified by the given {@code comparator}.
	 *
	 * @param comparator
	 * 		the comparator that defines the order of the elements
	 *
	 * @return a sorted list of this list's elements using the given comparator
	 */
	List<E> sorted(Comparator<? super E> comparator);
	
	List<E> distinct();
	
	List<E> distinct(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	@Override
	void clear();

	/**
	 * Ensures the capacity of this list to be at least {@code minSize}. This can be used to avoid having to recreate
	 * arrays in {@link ArrayList}s when the amount of elements to be added is already known.
	 *
	 * @param minSize
	 * 		the minimum size
	 */
	default void ensureCapacity(int minSize)
	{
	}

	/**
	 * Updates the element at the given {@code index} of this list. The default implementation of this method delegates
	 * to {@link #set(int, Object)}.
	 *
	 * @param index
	 * 		the index of the element to be updated
	 * @param element
	 * 		the new element
	 *
	 * @throws IndexOutOfBoundsException
	 * 		if the index is out of the bounds of this list
	 */
	default void subscript_$eq(int index, E element)
	{
		this.set(index, element);
	}

	default void subscript_$eq(Range<Integer> range, E[] elements)
	{
		int start = range.first();
		int length = range.count();
		for (int i = 0; i < length; i++)
		{
			this.subscript_$eq(start, elements[i]);
		}
	}

	default void subscript_$eq(Range<Integer> range, List<? extends E> elements)
	{
		int start = range.first();
		int length = range.last() - start + 1;
		for (int i = 0; i < length; i++)
		{
			this.subscript_$eq(start, elements.subscript(i));
		}
	}

	/**
	 * Updates the element at the given {@code index} of this list. This method throws an {@link
	 * IndexOutOfBoundsException} if the given {@code index} is less than {@code 0} or greater than or equal to the size
	 * of this list.
	 *
	 * @param index
	 * 		the index of the element to be updated
	 * @param element
	 * 		the new element
	 *
	 * @return the old element at the given index
	 *
	 * @throws IndexOutOfBoundsException
	 * 		if the index is out of the bounds of this list
	 */
	E set(int index, E element);

	/**
	 * Updates the element at the given {@code index} of this list. Unlike {@link #set(int, Object)}, this method will
	 * not throw any exceptions if the given {@code index} is out of bounds. Instead, it resizes the list to the
	 * required size and returns {@code null}.
	 *
	 * @param index
	 * 		the index of the element to be updated
	 * @param element
	 * 		the new element
	 *
	 * @return the old element, if present, {@code null} otherwise
	 */
	E setResizing(int index, E element);

	/**
	 * Inserts the element at the given {@code index} of this list. This method throws an {@link
	 * IndexOutOfBoundsException} if the given {@code index} is less than {@code 0} or greater than the size of this
	 * list. If the index is equal to the size of the list, the element gets appended to the end.
	 *
	 * @param index
	 * 		the index at which to insert the element
	 * @param element
	 * 		the element to be inserted
	 *
	 * @throws IndexOutOfBoundsException
	 * 		if the index is out of the bounds of this list
	 */
	void insert(int index, E element);

	/**
	 * Inserts the element at the given {@code index} of this list. Unlike {@link #insert(int, Object)}, this method
	 * will not throw any exception if the given {@code index} is out of bounds. Instead, it simply resizes this list to
	 * it's needs and returns {@code null}.
	 *
	 * @param index
	 * 		the index at which to insert the element
	 * @param element
	 * 		the element to be inserted
	 */
	default void insertResizing(int index, E element)
	{
		if (index > this.size())
		{
			this.setResizing(index, element);
			return;
		}
		this.insert(index, element);
	}

	void addElement(E element);

	@Override
	default boolean add(E element)
	{
		this.addElement(element);
		return true;
	}

	/**
	 * {@inheritDoc} Since {@link List Lists} can contain that same element multiple times, implementations should
	 * behave so that <i>all</i> occurrences of the element are removed, not only the first one. This behavior can be
	 * achieved using the {@link #removeFirst(Object) removeFirst} method.
	 */
	@Override
	boolean remove(Object element);

	/**
	 * Removes the first occurrence of the given {@code element} from this list. Unlike {@link #remove(Object)}, this
	 * methods removes at most one occurrence. Iff the element was found within this list, it is removed and {@code
	 * true} is returned. Otherwise, this methods returns {@code false}.
	 *
	 * @param element
	 * 		the element to remove
	 *
	 * @return true, if the element was found
	 */
	default boolean removeFirst(Object element)
	{
		final int index = this.indexOf(element);
		if (index < 0)
		{
			return false;
		}
		
		this.removeAt(index);
		return true;
	}

	/**
	 * Removes the last occurence of the given {@code element} from this list. Unlike {@link #remove(Object)}, this
	 * methods removes at most one occurrence. Iff the element was found within this list, it is removed and {@code
	 * true} is returned. Otherwise, this methods returns {@code false}.
	 *
	 * @param element
	 * 		the element to remove
	 *
	 * @return true, if the element was found
	 */
	default boolean removeLast(Object element)
	{
		final int index = this.lastIndexOf(element);
		if (index < 0)
		{
			return false;
		}
		
		this.removeAt(index);
		return true;
	}
	
	/**
	 * Removes the element at the given {@code index} from this list. This method throws an {@link
	 * IndexOutOfBoundsException} if the given {@code index} is less than {@code 0} or greater than or equal to the size
	 * of this list.
	 *
	 * @param index
	 * 		the index of the element to remove from this list
	 */
	void removeAt(int index);
	
	@Override
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);

	/**
	 * Reverses the elements in this list.
	 */
	void reverse();
	
	/**
	 * Sorts the elements of this list. The sorting order is given by the <i>natural order</i> of the elements of this
	 * collection, i.e. the order specified by their {@link Comparable#compareTo(Object) compareTo} method. Thus, this
	 * method will fail if the elements in this collection do not implement {@link Comparable} interface.
	 */
	void sort();
	
	/**
	 * Sorts the elements of this list. The sorting order is specified by the given {@code comparator}.
	 *
	 * @param comparator
	 * 		the comparator that defines the order of the elements
	 */
	void sort(Comparator<? super E> comparator);

	/**
	 * Removes all duplicate elements from this list.
	 */
	void distinguish();

	/**
	 * Removes all duplicate elements from this list and sorts it using the given {@code comparator}.
	 *
	 * @param comparator
	 * 		the comparator used to sort the list.
	 */
	void distinguish(Comparator<? super E> comparator);
	
	// Search Operations
	
	/**
	 * Returns the first index of the given {@code element} in this list, and {@code -1} if the element is not present
	 * in this list.
	 *
	 * @param element
	 * 		the element to search
	 *
	 * @return the first index of the element
	 */
	int indexOf(Object element);
	
	/**
	 * Returns the last index of the given {@code element} in this list, and {@code -1} if the element is not present in
	 * this list.
	 *
	 * @param element
	 * 		the element to search
	 *
	 * @return the last index of the element
	 */
	int lastIndexOf(Object element);
	
	// Copying and Views
	
	@Override
	List<E> copy();

	@Override
	<RE> MutableList<RE> emptyCopy();

	@Override
	<RE> MutableList<RE> emptyCopy(int capacity);

	@Override
	MutableList<E> mutable();
	
	@Override
	MutableList<E> mutableCopy();
	
	@Override
	ImmutableList<E> immutable();
	
	@Override
	ImmutableList<E> immutableCopy();

	@Override
	<RE> ImmutableList.Builder<RE> immutableBuilder();

	@Override
	<RE> ImmutableList.Builder<RE> immutableBuilder(int capacity);

	@Override
	ImmutableList<E> view();
	
	@Override
	java.util.List<E> toJava();
	
	// Utility Methods

	static void rangeCheck(int index, int size)
	{
		if (index < 0)
		{
			throw new IndexOutOfBoundsException("List index out of bounds: index < 0: " + index + " < 0");
		}
		if (index >= size)
		{
			throw new IndexOutOfBoundsException("List index out of bounds: index >= size: " + index + " >= " + size);
		}
	}
	
	static <E> boolean listEquals(List<E> list, Object o)
	{
		return o instanceof List && listEquals(list, (List<E>) o);
	}
	
	static <E> boolean listEquals(List<E> c1, List<E> c2)
	{
		return c1.size() == c2.size() && Collection.orderedEquals(c1, c2);
	}
	
	static <E> int listHashCode(List<E> list)
	{
		int result = 1;
		for (Object o : list)
		{
			result = 31 * result + (o == null ? 0 : o.hashCode());
		}
		return result;
	}
}

package dyvil.collection;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.util.ImmutableException;

/**
 * The base class of all collection classes in the <i>Dyvil Collection
 * Framework</i>. This class provides several methods for processing, mutating,
 * querying and converting collections. There is a clear distinction between
 * mutable and immutable collections, as there is an (im)mutable counterpart for
 * almost every method in this base class.
 * <p>
 * This interface supports the {@link NilConvertible} annotation, meaning that a
 * declaration like
 * 
 * <pre>
 * Collection[String] c = nil
 * </pre>
 * 
 * Would create an empty list by calling {@link #apply()} and assign it to the
 * variable {@code c}.
 * <p>
 * Furthermore, since this interface also supports the {@link ArrayConvertible}
 * annotation, it is possible to create a collection using <i>Array
 * Expressions</i> in <i>Dyvil</i>, as shown in the below example.
 * 
 * <pre>
 * Collection[String] c = [ 1, 2, 3 ]
 * </pre>
 * 
 * @author Clashsoft
 * @param <E>
 *            the element type
 */
@NilConvertible(methodName = "fromNil")
@ArrayConvertible
public interface Collection<E> extends Queryable<E>
{
	/**
	 * Returns an empty, immutable collection. This method is primarily for use
	 * with the {@code nil} literal in <i>Dyvil</i>, and the exact type of the
	 * returned object is given by {@link ImmutableSet#apply()}.
	 * 
	 * @return an empty, immutable collection
	 */
	public static <E> ImmutableCollection<E> fromNil()
	{
		return ImmutableSet.apply();
	}
	
	/**
	 * Returns an empty, mutable collection. The exact type of the returned
	 * object is given by {@link MutableList#apply()}.
	 * 
	 * @return an empty, mutable list
	 */
	public static <E> MutableCollection<E> apply()
	{
		return MutableList.apply();
	}
	
	/**
	 * Returns an immutable collection containing all of the given
	 * {@code elements}. This method is primarily for use with <i>Array
	 * Expressions</i> in <i>Dyvil</i> and is defined by
	 * {@link ImmutableList#apply(Object...)}.
	 * 
	 * @param elements
	 *            the elements of the returned collection
	 * @return an immutable collection containing all of the given elements
	 */
	public static <E> ImmutableCollection<E> apply(E... elements)
	{
		return ImmutableList.apply(elements);
	}
	
	// Accessors
	
	/**
	 * Returns {@code true} iff this collection is immutable, i.e. the number
	 * and order of it's elements cannot be changed after it's creation.
	 * 
	 * @return true, iff this collection is immutable
	 */
	public boolean isImmutable();
	
	/**
	 * Returns the size of this collection, i.e. the number of elements
	 * contained in this collection.
	 * 
	 * @return the size of this collection
	 */
	@Override
	public int size();
	
	/**
	 * Returns true if and if only this collection is empty. The standard
	 * implementation defines a collection as empty if it's size as calculated
	 * by {@link #size()} is exactly {@code 0}.
	 * 
	 * @return true, if this collection is empty
	 */
	@Override
	public default boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	public default boolean isDistinct()
	{
		int size = this.size();
		if (size < 2)
		{
			return true;
		}
		
		Object[] array = this.toArray();
		return Set.isDistinct(array, size);
	}
	
	public default boolean isSorted()
	{
		if (this.size() < 2)
		{
			return true;
		}
		
		return iteratorSorted(this.iterator());
	}
	
	public default boolean isSorted(Comparator<? super E> comparator)
	{
		if (this.size() < 2)
		{
			return true;
		}
		
		return iteratorSorted(this.iterator(), comparator);
	}
	
	/**
	 * Creates and returns an {@link Iterator} over the elements of this
	 * collection.
	 * 
	 * @return an iterator over the elements of this collection
	 */
	@Override
	public Iterator<E> iterator();
	
	/**
	 * Creates and returns a {@link Spliterator} over the elements of this
	 * collection.
	 * 
	 * @return a spliterator over the elements of this collection
	 */
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.SIZED);
	}
	
	/**
	 * Returns true if and if only this collection contains the element
	 * specified by {@code element}.
	 * 
	 * @param element
	 *            the element
	 * @return true, if this collection contains the element
	 */
	public default boolean $qmark(Object element)
	{
		return this.contains(element);
	}
	
	/**
	 * Returns true if and if only this collection contains the element
	 * specified by {@code element}.
	 * 
	 * @param element
	 *            the element
	 * @return true, if this collection contains the element
	 */
	@Override
	public default boolean contains(Object element)
	{
		return iterableContains(this, element);
	}
	
	public default boolean intersects(Collection<?> collection)
	{
		if (collection.size() < this.size())
		{
			for (Object o : this)
			{
				if (collection.contains(o))
				{
					return true;
				}
			}
			
			return false;
		}
		
		for (Object o : collection)
		{
			if (this.contains(o))
			{
				return true;
			}
		}
		
		return false;
	}
	
	// Non-mutating Operations
	
	/**
	 * Returns a collection that contains all elements of this collection plus
	 * the element given by {@code element}.
	 * 
	 * @param element
	 *            the element to be added
	 * @return a collection that contains all elements of this collection plus
	 *         the given element
	 */
	public Collection<E> $plus(E element);
	
	/**
	 * Returns a collection that contains all elements of this collection plus
	 * all elements of the given {@code collection}.
	 * 
	 * @param collection
	 *            the collection of elements to be added
	 * @return a collection that contains all elements of this collection plus
	 *         all elements of the collection
	 */
	public Collection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	/**
	 * Returns a collection that contains all elements of this collection
	 * excluding the element given by {@code element}.
	 * 
	 * @param element
	 *            the element to be removed
	 * @return a collection that contains all elements of this collection
	 *         excluding the given element
	 */
	public Collection<E> $minus(Object element);
	
	/**
	 * Returns a collection that contains all elements of this collection
	 * excluding all elements of the given {@code collection}.
	 * 
	 * @param collection
	 *            the collection of elements to be removed
	 * @return a collection that contains all elements of this collection
	 *         excluding all elements of the collection
	 */
	public Collection<? extends E> $minus$minus(Collection<?> collection);
	
	/**
	 * Returns a collection that contains all elements of this collection that
	 * are present in the given collection.
	 * 
	 * @param collection
	 *            the collection of elements to be retained
	 * @return a collection that contains all elements of this collection that
	 *         are present in the given collection
	 */
	public Collection<? extends E> $amp(Collection<? extends E> collection);
	
	/**
	 * Returns a collection that is mapped from this collection by supplying
	 * each of this collection's elements to the given {@code mapper}, and
	 * adding the returned mappings to a new collection.
	 * 
	 * @param mapper
	 *            the mapping function
	 * @return a collection mapped by the mapping function
	 */
	public <R> Collection<R> mapped(Function<? super E, ? extends R> mapper);
	
	/**
	 * Returns a collection that is flat-mapped from this collection by
	 * supplying each of this collection's elements to the given {@code mapper},
	 * and adding all elements by iterating over the {@link Iterable} returned
	 * by the {@code mapper}
	 * 
	 * @param mapper
	 *            the mapping function
	 * @return a collection flat-mapped by the mapping function
	 */
	public <R> Collection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	/**
	 * Returns a collection that is filtered from this collection by filtering
	 * each of this collection's elements using the given {@code condition}.
	 * 
	 * @param condition
	 *            the filter condition predicate
	 * @return a collection filtered by the filter condition predicate
	 */
	public Collection<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	/**
	 * Adds the element given by {@code element} to this collection. This method
	 * should throw an {@link ImmutableException} if this is an immutable
	 * collection.
	 * 
	 * @param element
	 *            the element to be added
	 */
	public default void $plus$eq(E element)
	{
		this.add(element);
	}
	
	/**
	 * Adds all elements of the given {@code collection} to this collection.
	 * This method should throw an {@link ImmutableException} if this is an
	 * immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be added
	 */
	public default void $plus$plus$eq(Collection<? extends E> collection)
	{
		for (E e : collection)
		{
			this.$plus$eq(e);
		}
	}
	
	// Mutating Operations
	
	/**
	 * Removes the element given by {@code element} from this collection. This
	 * method should throw an {@link ImmutableException} if this is an immutable
	 * collection.
	 * 
	 * @param element
	 *            the element to be removed
	 */
	public default void $minus$eq(Object element)
	{
		this.remove(element);
	}
	
	/**
	 * Removes all elements of the given {@code collection} from this
	 * collection. This method should throw an {@link ImmutableException} if the
	 * callee is an immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be removed
	 */
	public default void $minus$minus$eq(Collection<?> collection)
	{
		for (Object e : collection)
		{
			this.$minus$eq(e);
		}
	}
	
	// Mutating Operations
	
	/**
	 * Removes all elements of this collection that are not present in the given
	 * {@code collection}. This method should throw an
	 * {@link ImmutableException} if this is an immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be retained
	 */
	public default void $amp$eq(Collection<? extends E> collection)
	{
		this.intersect(collection);
	}
	
	// Mutating Operations
	
	/**
	 * Clears this collection such that all elements of this collection are
	 * removed and {@link #size()} returns {@code 0} after an invocation of this
	 * method. Note that implementations of this method in classes using an
	 * {@code int size} field, the classes should not only set their
	 * {@code size} to {@code 0}, but also make sure to actually set all
	 * elements to {@code null}. That ensures that these elements do not stay in
	 * memory and are eventually garbage-collected.
	 */
	public void clear();
	
	/**
	 * Adds the element given by {@code element} to this collection and returns
	 * the {@code true} if it was not present in this collection, {@code false}
	 * otherwise (does not apply to {@link List Lists} as the element will
	 * always be appended at the end of the list, therefore always returning
	 * {@code false}). This method should throw an {@link ImmutableException} if
	 * this is an immutable collection.
	 * 
	 * @param element
	 *            the element to be added
	 * @return the old element
	 */
	public boolean add(E element);
	
	/**
	 * Adds all elements of the given {@code collection} to this collection.
	 * This method should throw an {@link ImmutableException} if this is an
	 * immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be added
	 */
	public default boolean addAll(Collection<? extends E> collection)
	{
		boolean added = false;
		for (E element : collection)
		{
			if (this.add(element))
			{
				added = true;
			}
		}
		return added;
	}
	
	/**
	 * Removes the given {@code element} from this collection. If the element is
	 * not present in this list, it is simply ignored and {@code false} is
	 * returned. Otherwise, if the element has been successfully removed,
	 * {@code true} is returned.
	 * 
	 * @param element
	 *            the element to be removed
	 * @return true, iff the element has been removed successfully, false
	 *         otherwise
	 */
	public boolean remove(Object element);
	
	/**
	 * Removes all elements of the given {@code collection} from this
	 * collection. This method should throw an {@link ImmutableException} if the
	 * callee is an immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be removed
	 */
	public default boolean removeAll(Collection<?> collection)
	{
		boolean removed = false;
		for (Object o : collection)
		{
			if (this.remove(o))
			{
				removed = true;
			}
		}
		return removed;
	}
	
	/**
	 * Removes all elements of this collection that are not present in the given
	 * {@code collection}. This method should throw an
	 * {@link ImmutableException} if this is an immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be retained
	 */
	public default boolean intersect(Collection<? extends E> collection)
	{
		boolean removed = false;
		Iterator<E> iterator = this.iterator();
		while (iterator.hasNext())
		{
			if (!collection.contains(iterator.next()))
			{
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}
	
	/**
	 * Maps the elements of this collection using the given {@code mapper}. This
	 * is done by supplying each of this collection's elements to the mapping
	 * function and replacing them with the result returned by it.
	 * 
	 * @param mapper
	 *            the mapping function
	 */
	@Override
	public void map(Function<? super E, ? extends E> mapper);
	
	/**
	 * Flat-maps the elements of this collection using the given {@code mapper}.
	 * This is done by supplying each of this collection's elements to the
	 * mapping function and replacing them with the all elements of the
	 * {@link Iterator} returned by it.
	 * 
	 * @param mapper
	 *            the mapping function
	 */
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	/**
	 * Filters the elements of this collection using the given {@code condition}
	 * . This is done by supplying each of this collection's elements to the
	 * predicate condition, and removing them if the predicate fails, i.e.
	 * returns {@code false}.
	 * 
	 * @param condition
	 *            the filter condition predicate
	 */
	@Override
	public default void filter(Predicate<? super E> condition)
	{
		Iterator<E> iterator = this.iterator();
		while (iterator.hasNext())
		{
			if (!condition.test(iterator.next()))
			{
				iterator.remove();
			}
		}
	}
	
	// toArray
	
	/**
	 * Creates and returns an array containing the elements of this collection.
	 * 
	 * @return an array of this collection's elements
	 */
	public default Object[] toArray()
	{
		Object[] array = new Object[this.size()];
		this.toArray(0, array);
		return array;
	}
	
	/**
	 * Creates and returns an array containing the elements of this collection.
	 * The type of the array is specified by the given {@code type} representing
	 * the {@link Class} object of it's component type. Note that this method
	 * requires the elements of this collection to be casted to the given
	 * component type using it's {@link Class#cast(Object) cast} method.
	 * 
	 * @param type
	 *            the array type
	 * @return an array containing this collection's elements
	 */
	public default E[] toArray(Class<E> type)
	{
		E[] array = (E[]) Array.newInstance(type, this.size());
		this.toArray(0, array);
		return array;
	}
	
	/**
	 * Stores all elements of this collection sequentially in the given
	 * {@code store} array, starting at the index {@code 0}. The order in which
	 * the element are added to the array is the same order in which they would
	 * appear in this collection's {@link #iterator()}. Note that this method
	 * usually doesn't do boundary checking on the array, so passing an array of
	 * insufficient size to hold all elements of this collection will likely
	 * result in an {@link ArrayIndexOutOfBoundsException}.
	 * 
	 * @param store
	 *            the array to store the elements in
	 */
	public default void toArray(Object[] store)
	{
		this.toArray(0, store);
	}
	
	/**
	 * Stores all elements of this collection sequentially in the given
	 * {@code store} array, starting at given {@code index}. The order in which
	 * the element are added to the array is the same order in which they would
	 * appear in this collection's {@link #iterator()}. Note that this method
	 * usually doesn't do boundary checking on the array, so passing an array of
	 * insufficient size to hold all elements of this collection will likely
	 * result in an {@link ArrayIndexOutOfBoundsException}.
	 * 
	 * @param index
	 *            the index in the array at which the first element of this
	 *            collection should be placed
	 * @param store
	 *            the array to store the elements in
	 */
	public default void toArray(int index, Object[] store)
	{
		for (E e : this)
		{
			store[index++] = e;
		}
	}
	
	// Copying and Views
	
	/**
	 * Creates a copy of this collection. The general contract of this method is
	 * that the type of the returned collection is the same as this collection's
	 * type, such that
	 * 
	 * <pre>
	 * c.getClass == c.copy.getClass
	 * </pre>
	 * 
	 * @return a copy of this collection
	 */
	public Collection<E> copy();
	
	/**
	 * Returns a mutable collection that contains the exact same elements as
	 * this collection. Already mutable collections should return themselves
	 * when this method is called on them, while immutable collections should
	 * return a copy that can be modified.
	 * 
	 * @return a mutable collection with the same elements as this collection
	 */
	public MutableCollection<E> mutable();
	
	/**
	 * Returns a mutable copy of this collection. For mutable collections, this
	 * method has the same result as the {@link #copy()} method, while for
	 * immutable collections, the result of this method is the equivalent of a
	 * call to {@link #mutable()}.
	 * 
	 * @return a mutable copy of this collection
	 */
	public MutableCollection<E> mutableCopy();
	
	/**
	 * Returns an immutable collection that contains the exact same elements as
	 * this collection. Already immutable collections should return themselves
	 * when this method is called on them, while mutable collections should
	 * return a copy that cannot be modified.
	 * 
	 * @return a immutable collection with the same elements as this collection
	 */
	public ImmutableCollection<E> immutable();
	
	/**
	 * Returns a immutable copy of this collection. For immutable collections,
	 * this method has the same result as the {@link #copy()} method, while for
	 * mutable collections, the result of this method is the equivalent of a
	 * call to {@link #mutable()}.
	 * 
	 * @return an immutable copy of this collection
	 */
	public ImmutableCollection<E> immutableCopy();
	
	/**
	 * Returns a view on the elements of this collection. Immutable collections
	 * return themselves, as they are already immutable and therefore already
	 * provide a view. Mutable collections return a special collection that
	 * references them, and modifications such as element addition or removal in
	 * the original collection are reflect in the view.
	 * 
	 * @return a view on the elements of this collection
	 */
	public ImmutableCollection<E> view();
	
	/**
	 * Returns the Java Collection Framework equivalent of this collection. The
	 * returned collection is not a view of this one, but an exact copy.
	 * Immutable collections should return a collection that is locked for
	 * mutation, which is usually ensured by wrapping the collection with
	 * {@link java.util.Collections#unmodifiableCollection(java.util.Collection)
	 * Collections.unmodifiableCollection} .
	 * 
	 * @return a java collection containing the elements of this collection
	 */
	public java.util.Collection<E> toJava();
	
	// toString, equals and hashCode
	
	@Override
	public String toString();
	
	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
	public static <E> boolean iterableContains(Iterable<E> iterable, Object element)
	{
		if (element == null)
		{
			for (E e : iterable)
			{
				if (e == null)
				{
					return true;
				}
			}
			return false;
		}
		
		for (E e : iterable)
		{
			if (element.equals(e))
			{
				return true;
			}
		}
		return false;
	}
	
	public static <E> String collectionToString(Collection<E> collection)
	{
		if (collection.isEmpty())
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[");
		Iterator<E> iterator = collection.iterator();
		while (true)
		{
			E element = iterator.next();
			builder.append(element);
			if (iterator.hasNext())
			{
				builder.append(", ");
			}
			else
			{
				break;
			}
		}
		
		return builder.append(']').toString();
	}
	
	public static <E> boolean orderedEquals(Collection<E> c1, Collection<E> c2)
	{
		if (c1.size() != c2.size())
		{
			return false;
		}
		
		Iterator<E> iterator1 = c1.iterator();
		Iterator<E> iterator2 = c2.iterator();
		while (iterator1.hasNext())
		{
			E e1 = iterator1.next();
			E e2 = iterator2.next();
			if (!Objects.equals(e1, e2))
			{
				return false;
			}
		}
		return true;
	}
	
	public static <E> boolean unorderedEquals(Collection<E> c1, Collection<E> c2)
	{
		if (c1.size() != c2.size())
		{
			return false;
		}
		
		for (Object o : c1)
		{
			if (!c2.contains(o))
			{
				return c2.contains(o);
			}
		}
		return true;
	}
	
	public static <E> boolean isSorted(E[] array, int size)
	{
		if (size < 2)
		{
			return true;
		}
		
		for (int i = 1; i < size; i++)
		{
			if (((Comparable) array[i - 1]).compareTo(array[i]) > 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public static <E> boolean isSorted(E[] array, int size, Comparator<? super E> comparator)
	{
		if (size < 2)
		{
			return true;
		}
		
		for (int i = 1; i < size; i++)
		{
			if (comparator.compare(array[i - 1], array[i]) > 0)
			{
				return false;
			}
		}
		return true;
	}
	
	public static <E> boolean iteratorSorted(Iterator<E> iterator)
	{
		Comparable<? super E> prev = (Comparable<? super E>) iterator.next();
		while (iterator.hasNext())
		{
			E next = iterator.next();
			if (prev.compareTo(next) > 0)
			{
				return false;
			}
			prev = (Comparable<? super E>) next;
		}
		return true;
	}
	
	public static <E> boolean iteratorSorted(Iterator<E> iterator, Comparator<? super E> comparator)
	{
		E prev = iterator.next();
		while (iterator.hasNext())
		{
			E next = iterator.next();
			if (comparator.compare(prev, next) > 0)
			{
				return false;
			}
			prev = next;
		}
		return true;
	}
}

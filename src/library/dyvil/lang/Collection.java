package dyvil.lang;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableCollection;
import dyvil.collection.ImmutableList;
import dyvil.collection.MutableCollection;
import dyvil.collection.MutableList;

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
@NilConvertible
@ArrayConvertible
public interface Collection<E> extends Iterable<E>
{
	/**
	 * Returns an empty, mutable collection. This method is primarily for use
	 * with the {@code nil} literal in <i>Dyvil</i> and is defined by
	 * {@link MutableList#apply()}.
	 * 
	 * @see MutableList#apply()
	 * @return an empty, mutable collection
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
	 * Returns the size of this collection, i.e. the number of elements
	 * contained in this collection.
	 * 
	 * @return the size of this collection
	 */
	public int size();
	
	/**
	 * Returns true if and if only this collection is empty. The standard
	 * implementation defines a collection as empty if it's size as calculated
	 * by {@link #size()} is exactly {@code 0}.
	 * 
	 * @return true, if this collection is empty
	 */
	public default boolean isEmpty()
	{
		return this.size() == 0;
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
	
	public default Stream<E> stream()
	{
		return StreamSupport.stream(this.spliterator(), false);
	}
	
	public default Stream<E> parallelStream()
	{
		return StreamSupport.stream(this.spliterator(), true);
	}
	
	@Override
	public default void forEach(Consumer<? super E> action)
	{
		for (E element : this)
		{
			action.accept(element);
		}
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
	public default boolean contains(Object element)
	{
		return iterableContains(this, element);
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
	
	public default void toArray(Object[] store)
	{
		this.toArray(0, store);
	}
	
	public default void toArray(int index, Object[] store)
	{
		for (E e : this)
		{
			store[index++] = e;
		}
	}
	
	// Copying
	
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
	 * Returns a mutable copy of this collection. Already mutable collections
	 * should return themselves when this method is called on them, while
	 * immutable collections should return a copy that can be modified.
	 * 
	 * @return a mutable copy of this collection
	 */
	public MutableCollection<E> mutable();
	
	public MutableCollection<E> mutableCopy();
	
	/**
	 * Returns an immutable copy of this collection. Already immutable
	 * collections should return themselves when this method is called on them,
	 * while mutable collections should return a copy that cannot be modified.
	 * 
	 * @return an immutable copy of this collection
	 */
	public ImmutableCollection<E> immutable();
	
	public ImmutableCollection<E> immutableCopy();
	
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
	
	public static <E> boolean orderedEquals(Iterable<E> c1, Iterable<E> c2)
	{
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
				return false;
			}
		}
		return true;
	}
}

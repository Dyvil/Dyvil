package dyvil.lang;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collection.ImmutableCollection;
import dyvil.collection.ImmutableList;
import dyvil.collection.MutableCollection;
import dyvil.collection.MutableList;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

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
	
	@Override
	public void forEach(Consumer<? super E> action);
	
	/**
	 * Returns true if and if only this collection contains the element
	 * specified by {@code element}.
	 * 
	 * @param element
	 *            the element
	 * @return true, if this collection contains the element
	 */
	public boolean $qmark(Object element);
	
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
	public Collection<E> $minus(E element);
	
	/**
	 * Returns a collection that contains all elements of this collection
	 * excluding all elements of the given {@code collection}.
	 * 
	 * @param collection
	 *            the collection of elements to be removed
	 * @return a collection that contains all elements of this collection
	 *         excluding all elements of the collection
	 */
	public Collection<? extends E> $minus$minus(Collection<? extends E> collection);
	
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
	 * Adds the element given by {@code element} to this collection and returns
	 * the old element, if any. This method should throw an
	 * {@link ImmutableException} if this is an immutable collection.
	 * 
	 * @param element
	 *            the element to be added
	 * @return the old element
	 */
	public E add(E element);
	
	/**
	 * Adds the element given by {@code element} to this collection. This method
	 * should throw an {@link ImmutableException} if this is an immutable
	 * collection.
	 * 
	 * @param element
	 *            the element to be added
	 */
	public void $plus$eq(E element);
	
	/**
	 * Adds all elements of the given {@code collection} to this collection.
	 * This method should throw an {@link ImmutableException} if this is an
	 * immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be added
	 */
	public void $plus$plus$eq(Collection<? extends E> collection);
	
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
	public boolean remove(E element);
	
	/**
	 * Removes the element given by {@code element} from this collection. This
	 * method should throw an {@link ImmutableException} if this is an immutable
	 * collection.
	 * 
	 * @param element
	 *            the element to be removed
	 */
	public void $minus$eq(E element);
	
	/**
	 * Removes all elements of the given {@code collection} from this
	 * collection. This method should throw an {@link ImmutableException} if the
	 * callee is an immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be removed
	 */
	public void $minus$minus$eq(Collection<? extends E> collection);
	
	/**
	 * Removes all elements of this collection that are not present in the given
	 * {@code collection}. This method should throw an
	 * {@link ImmutableException} if this is an immutable collection.
	 * 
	 * @param collection
	 *            the collection of elements to be retained
	 */
	public void $amp$eq(Collection<? extends E> collection);
	
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
	 * Maps the elements of this collection using the given {@code mapper}. This
	 * is done by supplying each of this collection's elements to the mapping
	 * function and replacing them with the result returned by it.
	 * 
	 * @param mapper
	 *            the mapping function
	 */
	public void map(UnaryOperator<E> mapper);
	
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
	public void filter(Predicate<? super E> condition);
	
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
	
	// Copying
	
	public default void toArray(Object[] store)
	{
		this.toArray(0, store);
	}
	
	public void toArray(int index, Object[] store);
	
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
	
	/**
	 * Returns an immutable copy of this collection. Already immutable
	 * collections should return themselves when this method is called on them,
	 * while mutable collections should return a copy that cannot be modified.
	 * 
	 * @return an immutable copy of this collection
	 */
	public ImmutableCollection<E> immutable();
}

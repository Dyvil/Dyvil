package dyvil.lang;

import dyvil.collections.immutable.ImmutableCollection;
import dyvil.collections.mutable.MutableCollection;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

public interface Collection<E> extends ArrayConvertible, NilConvertible
{
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
	 * Returns true if and if only this collection contains the element
	 * specified by {@code element}
	 * 
	 * @param element
	 *            the element
	 * @return true, if this collection contains the element
	 */
	public boolean $qmark(E element);
	
	/**
	 * Returns a collection that contains all elements of this collection plus
	 * the element given by {@code element}. It the depends on the type of this
	 * collection if this method returns a new collection or the element is
	 * simply added to this collection.
	 * 
	 * @param element
	 *            the element to be added
	 * @return a collection that contains all elements of this collection plus
	 *         the given element
	 */
	public Collection<E> $plus(E element);
	
	/**
	 * Returns a collection that contains all elements of this collection plus
	 * all elements of the given {@code collection}. It the depends on the type
	 * of this collection if this method returns a new collection or the
	 * elements are simply added to this collection.
	 * 
	 * @param collection
	 *            the collection of elements to be added
	 * @return a collection that contains all elements of this collection plus
	 *         all elements of the collection
	 */
	public Collection<? extends E> $plus(Collection<? extends E> collection);
	
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
	public void $plus$eq(Collection<? extends E> collection);
	
	/**
	 * Returns a collection that contains all elements of this collection
	 * excluding the element given by {@code element}. It the depends on the
	 * type of this collection if this method returns a new collection or the
	 * element is simply removed from this collection.
	 * 
	 * @param element
	 *            the element to be removed
	 * @return a collection that contains all elements of this collection
	 *         excluding the given element
	 */
	public Collection<E> $minus(E element);
	
	/**
	 * Returns a collection that contains all elements of this collection
	 * excluding all elements of the given {@code collection}. It the depends on
	 * the type of this collection if this method returns a new collection or
	 * the elements are simply removed from this collection.
	 * 
	 * @param collection
	 *            the collection of elements to be removed
	 * @return a collection that contains all elements of this collection
	 *         excluding all elements of the collection
	 */
	public Collection<? extends E> $minus(Collection<? extends E> collection);
	
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
	public void $minus$eq(Collection<? extends E> collection);
	
	/**
	 * Returns a collection that contains all elements of this collection that
	 * are present in the given collection. It the depends on the type of this
	 * collection if this method returns a new collection or the elements are
	 * simply removed from this collection.
	 * 
	 * @param collection
	 *            the collection of elements to be retained
	 * @return a collection that contains all elements of this collection that
	 *         are present in the given collection
	 */
	public Collection<? extends E> $amp(Collection<? extends E> collection);
	
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

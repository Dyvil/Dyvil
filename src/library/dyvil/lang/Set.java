package dyvil.lang;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.annotation.sealed;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
public interface Set<E> extends Collection<E>
{
	@sealed
	Object	VALUE	= new Object();
	
	public static <E> MutableSet<E> apply()
	{
		return MutableSet.apply();
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.DISTINCT);
	}
	
	@Override
	public boolean $qmark(Object element);
	
	// Non-mutating Operations
	
	@Override
	public Set<E> $plus(E element);
	
	/**
	 * {@inheritDoc} This operator represents the 'union' Set operation and
	 * delegates to {@link #$bar(Collection)}.
	 */
	@Override
	public default Set<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return this.$bar(collection);
	}
	
	@Override
	public Set<E> $minus(Object element);
	
	/**
	 * {@inheritDoc} This operator represents the 'subtract' Set operation.
	 */
	@Override
	public Set<? extends E> $minus$minus(Collection<? extends E> collection);
	
	/**
	 * {@inheritDoc} This operator represents the 'intersect' Set operation.
	 */
	@Override
	public Set<? extends E> $amp(Collection<? extends E> collection);
	
	/**
	 * Returns a collection that contains all elements of this collection plus
	 * all elements of the given {@code collection} that are not currently
	 * present in this collection. This operator represents the 'union' Set
	 * operation.
	 * 
	 * @param collection
	 *            the collection of elements to be added
	 * @return a collection that contains all elements of this collection plus
	 *         all elements in the given collection that are not present in this
	 *         collection.
	 */
	public Set<? extends E> $bar(Collection<? extends E> collection);
	
	/**
	 * Returns a collection that contains all elements that are present in
	 * either this or the given {@code collection}, but not in both. This
	 * operator represents the 'exclusive OR' Set operation.
	 * 
	 * @param collection
	 *            the collection
	 * @return a collection that contains all elements that are present in
	 *         either this or the given collection, but not in both.
	 */
	public Set<? extends E> $up(Collection<? extends E> collection);
	
	@Override
	public <R> Set<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> Set<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public Set<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	public boolean add(E element);
	
	@Override
	public boolean remove(E element);
	
	@Override
	public void $amp$eq(Collection<? extends E> collection);
	
	/**
	 * Adds all elements of the given {@code collection} if they are not already
	 * present in this set.
	 * 
	 * @param collection
	 *            the collection to add
	 */
	public void $bar$eq(Collection<? extends E> collection);
	
	/**
	 * Removes all elements of the given {@code collection} from this collection
	 * and adds those that are not currently present in this collection.
	 * 
	 * @param collection
	 *            the collection to XOR with
	 */
	public void $up$eq(Collection<? extends E> collection);
	
	@Override
	public void clear();
	
	@Override
	public void map(UnaryOperator<E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	public void filter(Predicate<? super E> condition);
	
	// toArray
	
	@Override
	public void toArray(int index, Object[] store);
	
	// Copying
	
	@Override
	public Set<E> copy();
	
	@Override
	public MutableSet<E> mutable();
	
	@Override
	public ImmutableSet<E> immutable();
	
	public static boolean setEquals(Set<?> set, Object o)
	{
		if (!(o instanceof Set))
		{
			return false;
		}
		
		return setEquals((Set) set, (Set) o);
	}
	
	public static boolean setEquals(Set<?> c1, Set<?> c2)
	{
		if (c1.size() != c2.size())
		{
			return false;
		}
		
		for (Object o : c1)
		{
			if (!c2.$qmark(o))
			{
				return false;
			}
		}
		return true;
	}
	
	public static int setHashCode(Set<?> set)
	{
		int sum = 0;
		int product = 1;
		for (Object o : set)
		{
			if (o == null)
			{
				continue;
			}
			int hash = o.hashCode();
			sum += hash;
			product *= hash;
		}
		return sum * 31 + product;
	}
}

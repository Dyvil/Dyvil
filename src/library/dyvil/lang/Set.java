package dyvil.lang;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.annotation.sealed;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public interface Set<E> extends Collection<E>
{
	@sealed
	Object	VALUE	= new Object();
	
	public static <E> MutableSet<E> apply()
	{
		return MutableSet.apply();
	}
	
	public static <E> ImmutableSet<E> apply(E... elements)
	{
		return ImmutableSet.apply(elements);
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
	
	public static <E> boolean setEquals(Set<E> set, Object o)
	{
		if (!(o instanceof Set))
		{
			return false;
		}
		
		return setEquals(set, (Set) o);
	}
	
	public static <E> boolean setEquals(Set<E> c1, Set<E> c2)
	{
		if (c1.size() != c2.size())
		{
			return false;
		}
		
		return Collection.unorderedEquals(c1, c2);
	}
	
	public static <E> int setHashCode(Set<E> set)
	{
		int sum = 0;
		int product = 1;
		for (E element : set)
		{
			if (element == null)
			{
				continue;
			}
			int hash = element.hashCode();
			sum += hash;
			product *= hash;
		}
		return sum * 31 + product;
	}
	
	public static @sealed int distinct(Object[] array, int size)
	{
		if (size < 2)
		{
			return size;
		}
		
		for (int i = 0; i < size; i++)
		{
			for (int j = i + 1; j < size; j++)
			{
				if (Objects.equals(array[i], array[j]))
				{
					array[j--] = array[--size];
				}
			}
		}
		return size;
	}
	
	public static @sealed <T> int distinct(T[] array, int size, Comparator<? super T> comparator)
	{
		if (size < 2)
		{
			return size;
		}
		
		Arrays.sort(array, comparator);
		
		return distinctSorted(array, size);
	}
	
	public static @sealed int distinctSorted(Object[] array, int size)
	{
		if (size < 2)
		{
			return size;
		}
		
		int len = 0;
		int i = 1;
		
		while (i < size)
		{
			if (Objects.equals(array[i], array[len]))
			{
				i++;
			}
			else
			{
				array[++len] = array[i++];
			}
		}
		
		return len + 1;
	}
}

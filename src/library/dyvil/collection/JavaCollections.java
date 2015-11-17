package dyvil.collection;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.annotation._internal.infix;
import dyvil.annotation._internal.inline;
import dyvil.collection.mutable.ArrayList;

public interface JavaCollections
{
	// Access Operations
	
	/**
	 * @see Collection#contains(Object)
	 */
	static @infix @inline boolean $qmark(java.util.Collection<?> collection, Object o)
	{
		return collection.contains(o);
	}
	
	/**
	 * @see List#subscript(Object)
	 */
	static @infix @inline <E> E subscript(java.util.List<E> list, int index)
	{
		return list.get(index);
	}
	
	// Mutating Operations
	
	/**
	 * @see List#subscript_$eq(int, Object)
	 */
	static @infix @inline <E> void subscript_$eq(java.util.List<E> list, int index, E element)
	{
		list.set(index, element);
	}
	
	/**
	 * @see Collection#$plus$eq(Object)
	 */
	static @infix @inline <E> void $plus$eq(java.util.Collection<E> collection, E element)
	{
		collection.add(element);
	}
	
	/**
	 * @see Collection#$plus$plus$eq(Collection)
	 */
	static @infix @inline <E> void $plus$plus$eq(java.util.Collection<? super E> collection, java.util.Collection<? extends E> iterable)
	{
		collection.addAll(iterable);
	}
	
	/**
	 * @see Collection#$minus$eq(Entry)
	 */
	static @infix @inline void $minus$eq(java.util.Collection<?> collection, Object element)
	{
		collection.remove(element);
	}
	
	/**
	 * @see Collection#$minus$minus$eq(Map)
	 */
	static @infix @inline void $minus$minus$eq(java.util.Collection<?> collection, java.util.Collection<?> remove)
	{
		collection.removeAll(remove);
	}
	
	/**
	 * @see Collection#$amp$eq(Collection)
	 */
	static @infix @inline void $amp$eq(java.util.Collection<?> collection, java.util.Collection<?> retain)
	{
		collection.retainAll(retain);
	}
	
	/**
	 * @see Collection#map(Function)
	 */
	static @infix <E> void map(java.util.Collection<E> collection, Function<? super E, ? extends E> mapper)
	{
		int size = collection.size();
		java.util.Collection<E> list = new java.util.ArrayList(size);
		for (E element : collection)
		{
			list.add(mapper.apply(element));
		}
		collection.clear();
		collection.addAll(list);
	}
	
	/**
	 * @see Collection#flatMap(Function)
	 */
	static @infix <E> void flatMap(java.util.Collection<E> collection, Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		java.util.Collection<E> list = new java.util.LinkedList<>();
		for (E element : collection)
		{
			for (E e : mapper.apply(element))
			{
				list.add(e);
			}
		}
		collection.clear();
		collection.addAll(list);
	}
	
	/**
	 * @see Collection#filter(Predicate)
	 */
	static @infix <E> void filter(java.util.Collection<E> collection, Predicate<? super E> condition)
	{
		Iterator<E> iterator = collection.iterator();
		while (iterator.hasNext())
		{
			if (!condition.test(iterator.next()))
			{
				iterator.remove();
			}
		}
	}
	
	/**
	 * @see List#sort()
	 */
	static @infix @inline <E extends Comparable> void sort(java.util.List<E> list)
	{
		Collections.sort(list);
	}
	
	/**
	 * @see List#sort(Comparator)
	 */
	static @infix @inline <E> void sort(java.util.List<E> list, Comparator<? super E> comparator)
	{
		Collections.sort(list, comparator);
	}
	
	// Copying
	
	/**
	 * @see List#mutable()
	 */
	static @infix @inline <E> MutableList<E> mutable(java.util.List<E> list)
	{
		return new ArrayList(list.toArray(), true);
	}
	
	/**
	 * @see List#immutable()
	 */
	static @infix @inline <E> ImmutableList<E> immutable(java.util.List<E> list)
	{
		return new dyvil.collection.immutable.ArrayList(list.toArray(), true);
	}
}

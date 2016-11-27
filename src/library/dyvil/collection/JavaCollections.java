package dyvil.collection;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

public interface JavaCollections
{
	// Access Operations
	
	/**
	 * @see Collection#contains(Object)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static boolean $qmark(java.util.Collection<?> collection, Object o)
	{
		return collection.contains(o);
	}
	
	/**
	 * @see List#subscript(Object)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> E subscript(java.util.List<E> list, int index)
	{
		return list.get(index);
	}

	// Mutating Operations

	/**
	 * @see List#subscript_$eq(int, Object)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void subscript_$eq(java.util.List<E> list, int index, E element)
	{
		list.set(index, element);
	}

	/**
	 * @see Collection#$plus$eq(Object)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void $plus$eq(java.util.Collection<E> collection, E element)
	{
		collection.add(element);
	}

	/**
	 * @see Collection#$plus$plus$eq(Collection)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void $plus$plus$eq(java.util.Collection<? super E> collection, java.util.Collection<? extends E> iterable)
	{
		collection.addAll(iterable);
	}

	/**
	 * @see Collection#$minus$eq(Entry)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static void $minus$eq(java.util.Collection<?> collection, Object element)
	{
		collection.remove(element);
	}

	/**
	 * @see Collection#$minus$minus$eq(Map)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static void $minus$minus$eq(java.util.Collection<?> collection, java.util.Collection<?> remove)
	{
		collection.removeAll(remove);
	}

	/**
	 * @see Collection#$amp$eq(Collection)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static void $amp$eq(java.util.Collection<?> collection, java.util.Collection<?> retain)
	{
		collection.retainAll(retain);
	}

	/**
	 * @see Collection#map(Function)
	 */
	@DyvilModifiers(Modifiers.INFIX)
	static <E> void map(java.util.Collection<E> collection, Function<? super E, ? extends E> mapper)
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
	@DyvilModifiers(Modifiers.INFIX)
	static <E> void flatMap(java.util.Collection<E> collection, Function<? super E, ? extends Iterable<? extends E>> mapper)
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
	@DyvilModifiers(Modifiers.INFIX)
	static <E> void filter(java.util.Collection<E> collection, Predicate<? super E> condition)
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
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E extends Comparable> void sort(java.util.List<E> list)
	{
		Collections.sort(list);
	}

	/**
	 * @see List#sort(Comparator)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void sort(java.util.List<E> list, Comparator<? super E> comparator)
	{
		Collections.sort(list, comparator);
	}

	// Copying

	/**
	 * @see List#mutable()
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> MutableList<E> mutable(java.util.List<E> list)
	{
		return new ArrayList(list.toArray(), true);
	}

	/**
	 * @see List#immutable()
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> ImmutableList<E> immutable(java.util.List<E> list)
	{
		return new dyvil.collection.immutable.ArrayList(list.toArray(), true);
	}
}

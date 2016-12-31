package dyvil.collection;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
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
	static boolean $qmark(java.util.@NonNull Collection<?> collection, Object o)
	{
		return collection.contains(o);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> E subscript(java.util.@NonNull List<E> list, int index)
	{
		return list.get(index);
	}

	// Mutating Operations

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void subscript_$eq(java.util.@NonNull List<E> list, int index, E element)
	{
		list.set(index, element);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void $plus$eq(java.util.@NonNull Collection<E> collection, E element)
	{
		collection.add(element);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void $plus$plus$eq(java.util.@NonNull Collection<? super E> collection,
		                             java.util.@NonNull Collection<? extends E> iterable)
	{
		collection.addAll(iterable);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static void $minus$eq(java.util.@NonNull Collection<?> collection, Object element)
	{
		collection.remove(element);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static void $minus$minus$eq(java.util.@NonNull Collection<?> collection, java.util.@NonNull Collection<?> remove)
	{
		collection.removeAll(remove);
	}

	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static void $amp$eq(java.util.@NonNull Collection<?> collection, java.util.@NonNull Collection<?> retain)
	{
		collection.retainAll(retain);
	}

	/**
	 * @see Collection#map(Function)
	 */
	@DyvilModifiers(Modifiers.INFIX)
	static <E> void map(java.util.@NonNull Collection<E> collection, @NonNull Function<? super E, ? extends E> mapper)
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
	 * @see Queryable#flatMap(Function)
	 */
	@DyvilModifiers(Modifiers.INFIX)
	static <E> void flatMap(java.util.@NonNull Collection<E> collection,
		                       @NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
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
	static <E> void filter(java.util.@NonNull Collection<E> collection, @NonNull Predicate<? super E> condition)
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
	static <E extends Comparable<E>> void sort(java.util.@NonNull List<E> list)
	{
		Collections.sort(list);
	}

	/**
	 * @see List#sort(Comparator)
	 */
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> void sort(java.util.@NonNull List<E> list, Comparator<? super E> comparator)
	{
		list.sort(comparator);
	}

	// Copying

	/**
	 * @see List#mutable()
	 */
	@NonNull
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> MutableList<E> mutable(java.util.@NonNull List<E> list)
	{
		return new dyvil.collection.mutable.ArrayList(list.toArray(), true);
	}

	/**
	 * @see List#immutable()
	 */
	@NonNull
	@DyvilModifiers(Modifiers.INFIX | Modifiers.INLINE)
	static <E> ImmutableList<E> immutable(java.util.@NonNull List<E> list)
	{
		return new dyvil.collection.immutable.ArrayList(list.toArray(), true);
	}
}

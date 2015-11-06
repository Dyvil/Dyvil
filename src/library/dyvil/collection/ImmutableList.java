package dyvil.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.annotation.Covariant;
import dyvil.annotation.mutating;
import dyvil.collection.immutable.AppendList;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.immutable.EmptyList;
import dyvil.collection.immutable.SingletonList;
import dyvil.util.ImmutableException;

@NilConvertible
@ArrayConvertible
public interface ImmutableList<@Covariant E> extends List<E>, ImmutableCollection<E>
{
	public static interface Builder<E> extends ImmutableCollection.Builder<E>
	{
		@Override
		public ImmutableList<E> build();
	}
	
	public static <E> ImmutableList<E> apply()
	{
		return EmptyList.apply();
	}
	
	public static <E> ImmutableList<E> apply(E element)
	{
		return new SingletonList<E>(element);
	}
	
	public static <E> ImmutableList<E> apply(E e1, E e2)
	{
		return new ArrayList(new Object[] { e1, e2 }, 2, true);
	}
	
	public static <E> ImmutableList<E> apply(E e1, E e2, E e3)
	{
		return new ArrayList(new Object[] { e1, e2, e3 }, 3, true);
	}
	
	public static <E> ImmutableList<E> apply(E... elements)
	{
		return new ArrayList(elements, true);
	}
	
	public static <E> ImmutableList<E> apply(int count, E repeatedValue)
	{
		Object[] elements = new Object[count];
		for (int i = 0; i < count; i++)
		{
			elements[i] = repeatedValue;
		}
		return new ArrayList(elements, count, true);
	}
	
	public static <E> ImmutableList<E> apply(int count, IntFunction<E> generator)
	{
		Object[] elements = new Object[count];
		for (int i = 0; i < count; i++)
		{
			elements[i] = generator.apply(i);
		}
		return new ArrayList(elements, count, true);
	}
	
	public static <E> ImmutableList<E> fromArray(E... elements)
	{
		return new ArrayList(elements);
	}
	
	public static <E> ImmutableList<E> linked(E... elements)
	{
		ImmutableList<E> list = EmptyList.instance;
		for (E element : elements)
		{
			list = new AppendList(list, element);
		}
		return list;
	}
	
	public static <E> ImmutableList<E> linked(Iterable<E> iterable)
	{
		ImmutableList<E> list = EmptyList.instance;
		for (E element : iterable)
		{
			list = new AppendList(list, element);
		}
		return list;
	}
	
	public static <E> Builder<E> builder()
	{
		return new ArrayList.Builder();
	}
	
	public static <E> Builder<E> builder(int capacity)
	{
		return new ArrayList.Builder(capacity);
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public Iterator<E> reverseIterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.SIZED | Spliterator.IMMUTABLE);
	}
	
	@Override
	public E subscript(int index);
	
	@Override
	public E get(int index);
	
	// Non-mutating Operations
	
	@Override
	public ImmutableList<E> subList(int startIndex, int length);
	
	@Override
	public ImmutableList<E> $plus(E element);
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public ImmutableList<E> $minus(Object element);
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection);
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition);
	
	@Override
	public ImmutableList<E> reversed();
	
	@Override
	public ImmutableList<E> sorted();
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator);
	
	@Override
	public ImmutableList<E> distinct();
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	@Override
	@mutating
	public default void $plus$eq(E element)
	{
		throw new ImmutableException("+= on Immutable List");
	}
	
	@Override
	@mutating
	public default void $plus$plus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("++= on Immutable List");
	}
	
	@Override
	@mutating
	public default void $minus$eq(Object element)
	{
		throw new ImmutableException("-= on Immutable List");
	}
	
	@Override
	@mutating
	public default void $minus$minus$eq(Collection<?> collection)
	{
		throw new ImmutableException("--= on Immutable List");
	}
	
	@Override
	@mutating
	public default void $amp$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("&= on Immutable List");
	}
	
	@Override
	@mutating
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable List");
	}
	
	@Override
	@mutating
	public default void ensureCapacity(int minSize)
	{
	}
	
	@Override
	@mutating
	public default void subscript_$eq(int index, E element)
	{
		throw new ImmutableException("update() on Immutable List");
	}
	
	@Override
	@mutating
	public default E set(int index, E element)
	{
		throw new ImmutableException("set() on Immutable List");
	}
	
	@Override
	@mutating
	public default void insert(int index, E element)
	{
		throw new ImmutableException("insert() on Immutable List");
	}
	
	@Override
	@mutating
	public default boolean add(E element)
	{
		throw new ImmutableException("add() on Immutable List");
	}
	
	@Override
	@mutating
	public default E add(int index, E element)
	{
		throw new ImmutableException("add() on Immutable List");
	}
	
	@Override
	public default boolean addAll(Collection<? extends E> collection)
	{
		throw new ImmutableException("addAll() on Immutable List");
	}
	
	@Override
	@mutating
	public default boolean remove(Object element)
	{
		throw new ImmutableException("remove() on Immutable List");
	}
	
	@Override
	@mutating
	public default boolean removeFirst(Object element)
	{
		throw new ImmutableException("removeFirst() on Immutable List");
	}
	
	@Override
	@mutating
	public default boolean removeLast(Object element)
	{
		throw new ImmutableException("removeLast() on Immutable List");
	}
	
	@Override
	@mutating
	public default void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable List");
	}
	
	@Override
	@mutating
	public default boolean removeAll(Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable List");
	}
	
	@Override
	public default boolean intersect(Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable List");
	}
	
	@Override
	@mutating
	public default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable List");
	}
	
	@Override
	@mutating
	public default void map(Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable List");
	}
	
	@Override
	@mutating
	public default void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable List");
	}
	
	@Override
	@mutating
	public default void reverse()
	{
		throw new ImmutableException("reverse() on Immutable List");
	}
	
	@Override
	@mutating
	public default void sort()
	{
		throw new ImmutableException("sort() on Immutable List");
	}
	
	@Override
	@mutating
	public default void sort(Comparator<? super E> comparator)
	{
		throw new ImmutableException("sort() on Immutable List");
	}
	
	@Override
	@mutating
	public default void distinguish()
	{
		throw new ImmutableException("distinguish() on Immutable List");
	}
	
	@Override
	@mutating
	public default void distinguish(Comparator<? super E> comparator)
	{
		throw new ImmutableException("disinguish() on Immutable List");
	}
	
	// Searching
	
	@Override
	public int indexOf(Object element);
	
	@Override
	public int lastIndexOf(Object element);
	
	// Copying and Views
	
	@Override
	public ImmutableList<E> copy();
	
	@Override
	public MutableList<E> mutable();
	
	@Override
	public default MutableList<E> mutableCopy()
	{
		return this.mutable();
	}
	
	@Override
	public default ImmutableList<E> immutable()
	{
		return this;
	}
	
	@Override
	public default ImmutableList<E> immutableCopy()
	{
		return this.copy();
	}
	
	@Override
	public default ImmutableList<E> view()
	{
		return this;
	}
}

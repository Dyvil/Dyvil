package dyvil.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.annotation.mutating;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.immutable.EmptyList;
import dyvil.collection.immutable.SingletonList;
import dyvil.lang.Collection;
import dyvil.lang.ImmutableException;
import dyvil.lang.List;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
@ArrayConvertible
public interface ImmutableList<E> extends List<E>, ImmutableCollection<E>
{
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
	
	public static <E> ImmutableList<E> apply(E... array)
	{
		return new ArrayList(array);
	}
	
	public static <E> ImmutableList<E> apply(Iterable<E> iterable)
	{
		return MutableList.apply(iterable).immutable();
	}
	
	// Simple Getters
	
	@Override
	public int size();
	
	@Override
	public default boolean isEmpty()
	{
		return this.size() == 0;
	}
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	public void forEach(Consumer<? super E> action);
	
	@Override
	public boolean $qmark(Object element);
	
	@Override
	public E apply(int index);
	
	@Override
	public E get(int index);
	
	// Non-mutating Operations
	
	@Override
	public ImmutableList<E> slice(int startIndex, int length);
	
	@Override
	public ImmutableList<E> $plus(E element);
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public ImmutableList<E> $minus(E element);
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition);
	
	@Override
	public ImmutableList<E> sorted();
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator);
	
	// Mutating Operations
	
	@Override
	@mutating
	public default void resize(int newLength)
	{
		throw new ImmutableException("resize() on Immutable List");
	}
	
	@Override
	@mutating
	public default void ensureCapacity(int minSize)
	{
	}
	
	@Override
	@mutating
	public default void update(int index, E element)
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
	public default E add(E element)
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
	@mutating
	public default boolean remove(E element)
	{
		throw new ImmutableException("remove() on Immutable List");
	}
	
	@Override
	@mutating
	public default void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable List");
	}
	
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
	public default void $minus$eq(E element)
	{
		throw new ImmutableException("-= on Immutable List");
	}
	
	@Override
	@mutating
	public default void $minus$minus$eq(Collection<? extends E> collection)
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
	public default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable List");
	}
	
	@Override
	@mutating
	public default void map(UnaryOperator<E> mapper)
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
	
	// Searching
	
	@Override
	public int indexOf(E element);
	
	@Override
	public int lastIndexOf(E element);
	
	// toArray
	
	@Override
	public Object[] toArray();
	
	@Override
	public E[] toArray(Class<E> type);
	
	@Override
	public Object[] toArray(Object[] store);
	
	// Copying
	
	@Override
	public ImmutableList<E> copy();
	
	@Override
	public MutableList<E> mutable();
	
	@Override
	public default ImmutableList<E> immutable()
	{
		return this;
	}
}

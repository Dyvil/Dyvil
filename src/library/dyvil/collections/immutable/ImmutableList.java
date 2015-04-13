package dyvil.collections.immutable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collections.mutable.MutableList;
import dyvil.lang.Collection;
import dyvil.lang.ImmutableException;
import dyvil.lang.List;

public interface ImmutableList<E> extends List<E>, ImmutableCollection<E>
{
	public static <E> ImmutableList<E> apply()
	{
		return EmptyList.emptyList;
	}
	
	public static <E> ImmutableList<E> apply(E element)
	{
		return new SingletonList<E>(element);
	}
	
	public static <E> ImmutableList<E> apply(E e1, E e2)
	{
		return null; // FIXME
	}
	
	public static <E> ImmutableList<E> apply(E[] array)
	{
		return null; // FIXME
	}
	
	public static <E> ImmutableList<E> apply(Iterable<? extends E> iterable)
	{
		return null; // FIXME
	}
	
	// Simple Getters
	
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public Spliterator<E> spliterator();
	
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
	public ImmutableList<? extends E> $plus(Collection<? extends E> collection);
	
	@Override
	public ImmutableList<E> $minus(E element);
	
	@Override
	public ImmutableList<? extends E> $minus(Collection<? extends E> collection);
	
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
	public default void resize(int newLength)
	{
		throw new ImmutableException("resize() on Immutable List");
	}

	@Override
	public default void update(int index, E element)
	{
		throw new ImmutableException("update() on Immutable List");
	}

	@Override
	public default E set(int index, E element)
	{
		throw new ImmutableException("set() on Immutable List");
	}

	@Override
	public default void add(int index, E element)
	{
		throw new ImmutableException("add() on Immutable List");
	}

	@Override
	public default void remove(E element)
	{
		throw new ImmutableException("remove() on Immutable List");
	}

	@Override
	public default void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable List");
	}

	@Override
	public default void $plus$eq(E element)
	{
		throw new ImmutableException("+= on Immutable List");
	}
	
	@Override
	public default void $plus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("+= on Immutable List");
	}
	
	@Override
	public default void $minus$eq(E element)
	{
		throw new ImmutableException("-= on Immutable List");
	}
	
	@Override
	public default void $minus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("-= on Immutable List");
	}
	
	@Override
	public default void $amp$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("&= on Immutable List");
	}
	
	@Override
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable List");
	}
	
	@Override
	public default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable List");
	}
	
	@Override
	public default void map(UnaryOperator<E> mapper)
	{
		throw new ImmutableException("map() on Immutable List");
	}
	
	@Override
	public default void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable List");
	}
	
	@Override
	public default void sort()
	{
		throw new ImmutableException("sort() on Immutable List");
	}
	
	@Override
	public default void sort(Comparator<? super E> comparator)
	{
		throw new ImmutableException("sort() on Immutable List");
	}
	
	// Searching
	
	@Override
	public int indexOf(E element);
	
	@Override
	public int lastIndexOf(E element);
	
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

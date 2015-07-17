package dyvil.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.NilConvertible;

import dyvil.annotation.Covariant;
import dyvil.annotation.mutating;
import dyvil.util.Immutable;
import dyvil.util.ImmutableException;

@NilConvertible
public interface ImmutableCollection<@Covariant E> extends Collection<E>, Immutable
{
	public static <E> ImmutableCollection<E> apply()
	{
		return ImmutableList.apply();
	}
	
	// Accessors
	
	@Override
	public default boolean isImmutable()
	{
		return true;
	}
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	// Non-mutating Operations
	
	@Override
	public ImmutableCollection<E> $plus(E element);
	
	@Override
	public ImmutableCollection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<E> $minus(Object element);
	
	@Override
	public ImmutableCollection<? extends E> $minus$minus(Collection<?> collection);
	
	@Override
	public ImmutableCollection<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> ImmutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> ImmutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public ImmutableCollection<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	@mutating
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $plus$eq(E entry)
	{
		throw new ImmutableException("+= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $plus$plus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("++= on Immutable Collection");
	}
	
	// Mutating Operations
	
	@Override
	@mutating
	public default void $minus$eq(Object entry)
	{
		throw new ImmutableException("-= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $minus$minus$eq(Collection<?> collection)
	{
		throw new ImmutableException("--= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $amp$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("&= on Immutable Collection");
	}
	
	// Mutating Operations
	
	@Override
	@mutating
	public default boolean add(E element)
	{
		throw new ImmutableException("add() on Immutable Collection");
	}
	
	@Override
	public default boolean addAll(Collection<? extends E> collection)
	{
		throw new ImmutableException("addAll() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default boolean remove(Object element)
	{
		throw new ImmutableException("remove() on Immutable Collection");
	}
	
	@Override
	public default boolean removeAll(Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable Collection");
	}
	
	@Override
	public default boolean intersect(Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void map(Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable Collection");
	}
	
	// Copying
	
	@Override
	public ImmutableCollection<E> copy();
	
	@Override
	public MutableCollection<E> mutable();
	
	@Override
	public default MutableCollection<E> mutableCopy()
	{
		return this.mutable();
	}
	
	@Override
	public default ImmutableCollection<E> immutable()
	{
		return this;
	}
	
	@Override
	public default ImmutableCollection<E> immutableCopy()
	{
		return this.copy();
	}
	
	@Override
	public default ImmutableCollection<E> view()
	{
		return this;
	}
}

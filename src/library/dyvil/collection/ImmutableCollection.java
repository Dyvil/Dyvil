package dyvil.collection;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.annotation.mutating;
import dyvil.lang.Collection;
import dyvil.lang.Immutable;
import dyvil.lang.ImmutableException;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
public interface ImmutableCollection<E> extends Collection<E>, Immutable
{
	public static <E> ImmutableCollection<E> apply()
	{
		return ImmutableList.apply();
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	@Override
	public boolean $qmark(Object element);
	
	// Non-mutating Operations
	
	@Override
	public ImmutableCollection<E> $plus(E element);
	
	@Override
	public ImmutableCollection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<E> $minus(Object element);
	
	@Override
	public ImmutableCollection<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<? extends E> $bar(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<? extends E> $up(Collection<? extends E> collection);
	
	@Override
	public <R> ImmutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> ImmutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public ImmutableCollection<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	@mutating
	public default E add(E element)
	{
		throw new ImmutableException("add() on Immutable Collection");
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
	
	@Override
	@mutating
	public default boolean remove(E element)
	{
		throw new ImmutableException("remove() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $minus$eq(E entry)
	{
		throw new ImmutableException("-= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $minus$minus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("--= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $amp$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("&= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $bar$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("|= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void $up$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("^= on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable Collection");
	}
	
	@Override
	@mutating
	public default void map(UnaryOperator<E> mapper)
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
	
	// toArray
	
	@Override
	public void toArray(int index, Object[] store);
	
	// Copying
	
	@Override
	public ImmutableCollection<E> copy();
	
	@Override
	public MutableCollection<E> mutable();
	
	@Override
	public default ImmutableCollection<E> immutable()
	{
		return this;
	}
}

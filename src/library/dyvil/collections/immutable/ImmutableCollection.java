package dyvil.collections.immutable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import dyvil.collections.mutable.MutableCollection;
import dyvil.lang.Collection;
import dyvil.lang.Immutable;
import dyvil.lang.ImmutableException;

public interface ImmutableCollection<E> extends Collection<E>, Immutable
{
	@Override
	public int size();
	
	@Override
	public boolean isEmpty();
	
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
	public ImmutableCollection<E> $plus(E element);
	
	@Override
	public ImmutableCollection<? extends E> $plus$plus(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<E> $minus(E element);
	
	@Override
	public ImmutableCollection<? extends E> $minus$minus(Collection<? extends E> collection);
	
	@Override
	public ImmutableCollection<? extends E> $amp(Collection<? extends E> collection);
	
	@Override
	public <R> ImmutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	public <R> ImmutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	public ImmutableCollection<E> filtered(Predicate<? super E> condition);
	
	@Override
	public ImmutableCollection<E> sorted();
	
	@Override
	public ImmutableCollection<E> sorted(Comparator<? super E> comparator);
	
	@Override
	public default E add(E element)
	{
		throw new ImmutableException("add() on Immutable Collection");
	}
	
	@Override
	public default void $plus$eq(E entry)
	{
		throw new ImmutableException("+= on Immutable Collection");
	}
	
	@Override
	public default void $plus$plus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("++= on Immutable Collection");
	}
	
	@Override
	public default boolean remove(E element)
	{
		throw new ImmutableException("remove() on Immutable Collection");
	}
	
	@Override
	public default void $minus$eq(E entry)
	{
		throw new ImmutableException("-= on Immutable Collection");
	}
	
	@Override
	public default void $minus$minus$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("--= on Immutable Collection");
	}
	
	@Override
	public default void $amp$eq(Collection<? extends E> collection)
	{
		throw new ImmutableException("&= on Immutable Collection");
	}
	
	@Override
	public default void clear()
	{
		throw new ImmutableException("clear() on Immutable Collection");
	}
	
	@Override
	public default void map(UnaryOperator<E> mapper)
	{
		throw new ImmutableException("map() on Immutable Collection");
	}
	
	@Override
	public default void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Collection");
	}
	
	@Override
	public default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable Collection");
	}
	
	@Override
	public default void sort()
	{
		throw new ImmutableException("sort() on Immutable Collection");
	}
	
	@Override
	public default void sort(Comparator<? super E> comparator)
	{
		throw new ImmutableException("sort() on Immutable Collection");
	}
	
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

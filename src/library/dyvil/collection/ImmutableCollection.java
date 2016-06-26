package dyvil.collection;

import dyvil.annotation.Immutable;
import dyvil.annotation.Mutating;
import dyvil.annotation._internal.Covariant;
import dyvil.util.ImmutableException;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;

@Immutable
public interface ImmutableCollection<@Covariant E> extends Collection<E>
{
	interface Builder<E>
	{
		void add(E element);
		
		default void addAll(Iterable<? extends E> elements)
		{
			for (E element : elements)
			{
				this.add(element);
			}
		}
		
		ImmutableCollection<E> build();
	}
	
	// Accessors
	
	@Override
	default boolean isImmutable()
	{
		return true;
	}
	
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	@Override
	default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}
	
	// Non-mutating Operations
	
	@Override
	ImmutableCollection<E> added(E element);
	
	@Override
	ImmutableCollection<E> union(Collection<? extends E> collection);
	
	@Override
	ImmutableCollection<E> removed(Object element);
	
	@Override
	ImmutableCollection<E> difference(Collection<?> collection);
	
	@Override
	ImmutableCollection<E> intersection(Collection<? extends E> collection);
	
	@Override
	<R> ImmutableCollection<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	<R> ImmutableCollection<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	ImmutableCollection<E> filtered(Predicate<? super E> condition);
	
	// Mutating Operations
	
	@Override
	@Mutating
	default void clear()
	{
		throw new ImmutableException("clear() on Immutable Collection");
	}
	
	@Override
	@Mutating
	default boolean add(E element)
	{
		throw new ImmutableException("add() on Immutable Collection");
	}
	
	@Override
	default boolean addAll(Collection<? extends E> collection)
	{
		throw new ImmutableException("addAll() on Immutable Collection");
	}
	
	@Override
	@Mutating
	default boolean remove(Object element)
	{
		throw new ImmutableException("remove() on Immutable Collection");
	}
	
	@Override
	default boolean removeAll(Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable Collection");
	}
	
	@Override
	default boolean retainAll(Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable Collection");
	}
	
	@Override
	@Mutating
	default void map(Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable Collection");
	}
	
	@Override
	@Mutating
	default void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Collection");
	}
	
	@Override
	@Mutating
	default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable Collection");
	}
	
	// Copying
	
	@Override
	ImmutableCollection<E> copy();
	
	@Override
	MutableCollection<E> mutable();
	
	@Override
	default MutableCollection<E> mutableCopy()
	{
		return this.mutable();
	}
	
	@Override
	default ImmutableCollection<E> immutable()
	{
		return this;
	}
	
	@Override
	default ImmutableCollection<E> immutableCopy()
	{
		return this.copy();
	}
	
	@Override
	default ImmutableCollection<E> view()
	{
		return this;
	}
}

package dyvil.collection;

import dyvil.annotation.Immutable;
import dyvil.annotation.Mutating;
import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
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

		default void addAll(@NonNull Iterable<? extends E> elements)
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

	@NonNull
	@Override
	Iterator<E> iterator();

	@NonNull
	@Override
	default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.IMMUTABLE);
	}

	// Non-mutating Operations

	@NonNull
	@Override
	ImmutableCollection<E> added(E element);

	@NonNull
	@Override
	ImmutableCollection<E> union(@NonNull Collection<? extends E> collection);

	@Override
	ImmutableCollection<E> removed(Object element);

	@Override
	ImmutableCollection<E> difference(@NonNull Collection<?> collection);

	@Override
	ImmutableCollection<E> intersection(@NonNull Collection<? extends E> collection);

	@NonNull
	@Override
	<R> ImmutableCollection<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	@NonNull
	@Override
	<R> ImmutableCollection<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper);

	@NonNull
	@Override
	ImmutableCollection<E> filtered(@NonNull Predicate<? super E> condition);

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
	default boolean addAll(@NonNull Collection<? extends E> collection)
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
	default boolean removeAll(@NonNull Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable Collection");
	}

	@Override
	default boolean retainAll(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable Collection");
	}

	@Override
	@Mutating
	default void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable Collection");
	}

	@Override
	@Mutating
	default void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Collection");
	}

	@Override
	@Mutating
	default void filter(@NonNull Predicate<? super E> condition)
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

	@NonNull
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

	@NonNull
	@Override
	default ImmutableCollection<E> view()
	{
		return this;
	}
}

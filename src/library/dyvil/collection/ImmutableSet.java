package dyvil.collection;

import dyvil.annotation.Deprecated;
import dyvil.annotation.Immutable;
import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.immutable.ArraySet;
import dyvil.collection.immutable.EmptySet;
import dyvil.collection.immutable.SingletonSet;
import dyvil.lang.LiteralConvertible;
import dyvil.util.ImmutableException;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;

@Deprecated(replacements = { "java.util.Set" })
@java.lang.Deprecated
@LiteralConvertible.FromArray
@Immutable
public interface ImmutableSet<@Covariant E> extends Set<E>, ImmutableCollection<E>
{
	interface Builder<E> extends ImmutableCollection.Builder<E>
	{
		@Override
		ImmutableSet<E> build();
	}

	@NonNull
	static <E> ImmutableSet<E> apply()
	{
		return EmptySet.apply();
	}

	@NonNull
	static <E> ImmutableSet<E> apply(E element)
	{
		return SingletonSet.apply(element);
	}

	@SafeVarargs
	static <E> ImmutableSet<E> apply(E... elements)
	{
		return ArraySet.apply(elements);
	}

	static <E> ImmutableSet<E> from(E[] array)
	{
		return ArraySet.from(array);
	}

	static <E> ImmutableSet<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return ArraySet.from(iterable);
	}

	static <E> ImmutableSet<E> from(@NonNull Collection<? extends E> collection)
	{
		return ArraySet.from(collection);
	}

	@NonNull
	static <E> Builder<E> builder()
	{
		return new ArraySet.Builder<>();
	}

	@NonNull
	static <E> Builder<E> builder(int capacity)
	{
		return new ArraySet.Builder<>(capacity);
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
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.DISTINCT | Spliterator.IMMUTABLE);
	}

	// Non-mutating Operations

	@NonNull
	@Override
	ImmutableSet<E> added(E element);

	@Override
	ImmutableSet<E> removed(Object element);

	/**
	 * {@inheritDoc} This operator represents the 'union' ImmutableSet operation.
	 */
	@NonNull
	@Override
	ImmutableSet<E> union(@NonNull Collection<? extends E> collection);

	/**
	 * {@inheritDoc} This operator represents the 'subtract' ImmutableSet operation.
	 */
	@Override
	ImmutableSet<E> difference(@NonNull Collection<?> collection);

	/**
	 * {@inheritDoc} This operator represents the 'intersect' ImmutableSet operation.
	 */
	@Override
	ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection);

	/**
	 * {@inheritDoc} This operator represents the 'exclusive OR' ImmutableSet operation.
	 */
	@NonNull
	@Override
	ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection);

	@NonNull
	@Override
	<R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	@NonNull
	@Override
	<R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper);

	@NonNull
	@Override
	ImmutableSet<E> filtered(@NonNull Predicate<? super E> predicate);

	// Mutating Operations

	@Override
	default void clear()
	{
		throw new ImmutableException("clear() on Immutable Set");
	}

	@Override
	default boolean add(E element)
	{
		throw new ImmutableException("add() on Immutable Set");
	}

	@Override
	default boolean addAll(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("addAll() on Immutable Set");
	}

	@Override
	default boolean remove(Object element)
	{
		throw new ImmutableException("remove() on Immutable Set");
	}

	@Override
	default boolean removeAll(@NonNull Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable Set");
	}

	@Override
	default boolean retainAll(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable Set");
	}

	@Override
	default boolean unionInplace(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("union() on Immutable Set");
	}

	@Override
	default boolean symmetricDifferenceInplace(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("exclusiveOr() on Immutable Set");
	}

	@Override
	default void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable Set");
	}

	@Override
	default void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Set");
	}

	@Override
	default void filter(@NonNull Predicate<? super E> predicate)
	{
		throw new ImmutableException("filter() on Immutable Set");
	}

	// Copying

	@NonNull
	@Override
	ImmutableSet<E> copy();

	@Override
	<RE> MutableSet<RE> emptyCopy();

	@NonNull
	@Override
	<RE> MutableSet<RE> emptyCopy(int capacity);

	@NonNull
	@Override
	MutableSet<E> mutable();

	@NonNull
	@Override
	default MutableSet<E> mutableCopy()
	{
		return this.mutable();
	}

	@NonNull
	@Override
	default ImmutableSet<E> immutable()
	{
		return this;
	}

	@NonNull
	@Override
	default ImmutableSet<E> immutableCopy()
	{
		return this.copy();
	}

	@Override
	<RE> Builder<RE> immutableBuilder();

	@Override
	<RE> Builder<RE> immutableBuilder(int capacity);

	@NonNull
	@Override
	default ImmutableSet<E> view()
	{
		return this;
	}
}

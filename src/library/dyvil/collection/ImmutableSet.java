package dyvil.collection;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.Covariant;
import dyvil.collection.immutable.ArraySet;
import dyvil.collection.immutable.EmptySet;
import dyvil.collection.immutable.SingletonSet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.util.ImmutableException;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@ArrayConvertible
@Immutable
public interface ImmutableSet<@Covariant E> extends Set<E>, ImmutableCollection<E>
{
	interface Builder<E> extends ImmutableCollection.Builder<E>
	{
		@Override
		ImmutableSet<E> build();
	}
	
	static <E> ImmutableSet<E> apply()
	{
		return EmptySet.apply();
	}
	
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

	static <E> ImmutableSet<E> from(Iterable<? extends E> iterable)
	{
		return ArraySet.from(iterable);
	}

	static <E> ImmutableSet<E> from(Collection<? extends E> collection)
	{
		return ArraySet.from(collection);
	}

	static <E> Builder<E> builder()
	{
		return new ArraySet.Builder<>();
	}

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
	
	@Override
	Iterator<E> iterator();
	
	@Override
	default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.DISTINCT | Spliterator.IMMUTABLE);
	}
	
	// Non-mutating Operations
	
	@Override
	ImmutableSet<E> added(E element);
	
	@Override
	ImmutableSet<E> removed(Object element);

	/**
	 * {@inheritDoc} This operator represents the 'union' ImmutableSet operation.
	 */
	@Override
	ImmutableSet<? extends E> union(Collection<? extends E> collection);

	/**
	 * {@inheritDoc} This operator represents the 'subtract' ImmutableSet operation.
	 */
	@Override
	ImmutableSet<? extends E> difference(Collection<?> collection);

	/**
	 * {@inheritDoc} This operator represents the 'intersect' ImmutableSet operation.
	 */
	@Override
	ImmutableSet<? extends E> intersection(Collection<? extends E> collection);

	/**
	 * {@inheritDoc} This operator represents the 'exclusive OR' ImmutableSet operation.
	 */
	@Override
	ImmutableSet<? extends E> symmetricDifference(Collection<? extends E> collection);
	
	@Override
	<R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper);
	
	@Override
	<R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper);
	
	@Override
	ImmutableSet<E> filtered(Predicate<? super E> condition);
	
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
	default boolean addAll(Collection<? extends E> collection)
	{
		throw new ImmutableException("addAll() on Immutable Set");
	}
	
	@Override
	default boolean remove(Object element)
	{
		throw new ImmutableException("remove() on Immutable Set");
	}
	
	@Override
	default boolean removeAll(Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable Set");
	}
	
	@Override
	default boolean retainAll(Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable Set");
	}
	
	@Override
	default boolean unionInplace(Collection<? extends E> collection)
	{
		throw new ImmutableException("union() on Immutable Set");
	}
	
	@Override
	default boolean symmetricDifferenceInplace(Collection<? extends E> collection)
	{
		throw new ImmutableException("exclusiveOr() on Immutable Set");
	}
	
	@Override
	default void map(Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable Set");
	}
	
	@Override
	default void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable Set");
	}
	
	@Override
	default void filter(Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable Set");
	}
	
	// Copying
	
	@Override
	ImmutableSet<E> copy();

	@Override
	<RE> MutableSet<RE> emptyCopy();

	@Override
	<RE> MutableSet<RE> emptyCopy(int capacity);

	@Override
	MutableSet<E> mutable();
	
	@Override
	default MutableSet<E> mutableCopy()
	{
		return this.mutable();
	}
	
	@Override
	default ImmutableSet<E> immutable()
	{
		return this;
	}
	
	@Override
	default ImmutableSet<E> immutableCopy()
	{
		return this.copy();
	}

	@Override
	<RE> Builder<RE> immutableBuilder();

	@Override
	<RE> Builder<RE> immutableBuilder(int capacity);

	@Override
	default ImmutableSet<E> view()
	{
		return this;
	}
}

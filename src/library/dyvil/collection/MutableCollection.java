package dyvil.collection;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
public interface MutableCollection<E> extends Collection<E>
{
	@NonNull
	static <E> MutableCollection<E> apply()
	{
		return MutableList.apply();
	}

	@NonNull
	@SafeVarargs
	static <E> MutableCollection<E> apply(E... elements)
	{
		return MutableList.apply(elements);
	}

	// Accessors

	@Override
	default boolean isImmutable()
	{
		return false;
	}

	@Override
	int size();

	@NonNull
	@Override
	Iterator<E> iterator();

	// Non-mutating Operations

	@NonNull
	@Override
	MutableCollection<E> added(E element);

	@NonNull
	@Override
	MutableCollection<E> union(@NonNull Collection<? extends E> collection);

	@NonNull
	@Override
	MutableCollection<E> removed(Object element);

	@NonNull
	@Override
	MutableCollection<E> difference(@NonNull Collection<?> collection);

	@NonNull
	@Override
	MutableCollection<E> intersection(@NonNull Collection<? extends E> collection);

	@NonNull
	@Override
	<R> MutableCollection<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	@NonNull
	@Override
	<R> MutableCollection<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper);

	@NonNull
	@Override
	MutableCollection<E> filtered(@NonNull Predicate<? super E> condition);

	// Mutating Operations

	@Override
	void clear();

	@Override
	boolean add(E element);

	@Override
	boolean remove(Object element);

	@Override
	void map(@NonNull Function<? super E, ? extends E> mapper);

	@Override
	void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper);

	// Copying

	@NonNull
	@Override
	MutableCollection<E> copy();

	@NonNull
	@Override
	<R> MutableCollection<R> emptyCopy();

	@NonNull
	@Override
	<RE> MutableCollection<RE> emptyCopy(int capacity);

	@NonNull
	@Override
	default MutableCollection<E> mutable()
	{
		return this;
	}

	@NonNull
	@Override
	default MutableCollection<E> mutableCopy()
	{
		return this.copy();
	}

	@NonNull
	@Override
	ImmutableCollection<E> immutable();

	@NonNull
	@Override
	default ImmutableCollection<E> immutableCopy()
	{
		return this.immutable();
	}

	@Override
	<RE> ImmutableCollection.Builder<RE> immutableBuilder();

	@Override
	<RE> ImmutableCollection.Builder<RE> immutableBuilder(int capacity);

	@NonNull
	@Override
	ImmutableCollection<E> view();
}

package dyvil.collection;

import dyvil.annotation.Deprecated;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.mutable.ArraySet;
import dyvil.collection.mutable.HashSet;
import dyvil.collection.view.SetView;
import dyvil.lang.LiteralConvertible;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@Deprecated(replacements = { "java.util.Set" })
@java.lang.Deprecated
@LiteralConvertible.FromArray
public interface MutableSet<E> extends Set<E>, MutableCollection<E>
{
	@NonNull
	static <E> MutableSet<E> apply()
	{
		return new HashSet<>();
	}

	@NonNull
	static <E> MutableSet<E> withCapacity(int capacity)
	{
		return new HashSet<>(capacity);
	}

	@NonNull
	@SafeVarargs
	static <E> MutableSet<E> apply(E... elements)
	{
		return ArraySet.apply(elements);
	}

	@NonNull
	static <E> MutableSet<E> from(E[] array)
	{
		return ArraySet.from(array);
	}

	@NonNull
	static <E> MutableSet<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return ArraySet.from(iterable);
	}

	@NonNull
	static <E> MutableSet<E> from(@NonNull Collection<? extends E> collection)
	{
		return ArraySet.from(collection);
	}

	// Accessors

	@Override
	int size();

	@NonNull
	@Override
	Iterator<E> iterator();

	// Non-mutating Operations

	@NonNull
	@Override
	default MutableSet<E> added(E element)
	{
		MutableSet<E> copy = this.copy();
		copy.add(element);
		return copy;
	}

	@NonNull
	@Override
	default MutableSet<E> removed(Object element)
	{
		MutableSet<E> copy = this.copy();
		copy.remove(element);
		return copy;
	}

	@NonNull
	@Override
	default MutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.addAll(collection);
		return copy;
	}

	@NonNull
	@Override
	default MutableSet<E> difference(@NonNull Collection<?> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.removeAll(collection);
		return copy;
	}

	@NonNull
	@Override
	default MutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.retainAll(collection);
		return copy;
	}

	@NonNull
	@Override
	default MutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.symmetricDifferenceInplace(collection);
		return copy;
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	default <R> MutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	default <R> MutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.flatMap((Function) mapper);
		return copy;
	}

	@NonNull
	@Override
	default MutableSet<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		MutableSet<E> copy = this.copy();
		copy.filter(predicate);
		return copy;
	}

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
	MutableSet<E> copy();

	@NonNull
	@Override
	default MutableSet<E> mutable()
	{
		return this;
	}

	@NonNull
	@Override
	default MutableSet<E> mutableCopy()
	{
		return this.copy();
	}

	@NonNull
	@Override
	<R> MutableSet<R> emptyCopy();

	@NonNull
	@Override
	<RE> MutableSet<RE> emptyCopy(int capacity);

	@NonNull
	@Override
	ImmutableSet<E> immutable();

	@NonNull
	@Override
	default ImmutableSet<E> immutableCopy()
	{
		return this.immutable();
	}

	@Override
	<RE> ImmutableSet.Builder<RE> immutableBuilder();

	@Override
	<RE> ImmutableSet.Builder<RE> immutableBuilder(int capacity);

	@NonNull
	@Override
	default ImmutableSet<E> view()
	{
		return new SetView<>(this);
	}
}

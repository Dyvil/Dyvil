package dyvil.collection;

import dyvil.collection.mutable.ArraySet;
import dyvil.collection.mutable.HashSet;
import dyvil.collection.view.SetView;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@ArrayConvertible
public interface MutableSet<E> extends Set<E>, MutableCollection<E>
{
	static <E> MutableSet<E> apply()
	{
		return new HashSet<>();
	}

	static <E> MutableSet<E> withCapacity(int capacity)
	{
		return new HashSet<>(capacity);
	}
	
	@SafeVarargs
	static <E> MutableSet<E> apply(E... elements)
	{
		return ArraySet.apply(elements);
	}

	static <E> MutableSet<E> from(E[] array)
	{
		return ArraySet.from(array);
	}

	static <E> MutableSet<E> from(Iterable<? extends E> iterable)
	{
		return ArraySet.from(iterable);
	}

	static <E> MutableSet<E> from(Collection<? extends E> collection)
	{
		return ArraySet.from(collection);
	}
	
	// Accessors
	
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	// Non-mutating Operations
	
	@Override
	default MutableSet<E> added(E element)
	{
		MutableSet<E> copy = this.copy();
		copy.add(element);
		return copy;
	}
	
	@Override
	default MutableSet<E> removed(Object element)
	{
		MutableSet<E> copy = this.copy();
		copy.remove(element);
		return copy;
	}

	@Override
	default MutableSet<E> union(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.addAll(collection);
		return copy;
	}

	@Override
	default MutableSet<E> difference(Collection<?> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.removeAll(collection);
		return copy;
	}

	@Override
	default MutableSet<E> intersection(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.retainAll(collection);
		return copy;
	}
	
	@Override
	default MutableSet<E> symmetricDifference(Collection<? extends E> collection)
	{
		MutableSet<E> copy = this.copy();
		copy.symmetricDifferenceInplace(collection);
		return copy;
	}

	@Override
	@SuppressWarnings("unchecked")
	default <R> MutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	default <R> MutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		MutableSet<R> copy = (MutableSet<R>) this.copy();
		copy.flatMap((Function) mapper);
		return copy;
	}
	
	@Override
	default MutableSet<E> filtered(Predicate<? super E> condition)
	{
		MutableSet<E> copy = this.copy();
		copy.filter(condition);
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
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	// Copying
	
	@Override
	MutableSet<E> copy();
	
	@Override
	default MutableSet<E> mutable()
	{
		return this;
	}
	
	@Override
	default MutableSet<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	<R> MutableSet<R> emptyCopy();

	@Override
	<RE> MutableSet<RE> emptyCopy(int capacity);

	@Override
	ImmutableSet<E> immutable();
	
	@Override
	default ImmutableSet<E> immutableCopy()
	{
		return this.immutable();
	}

	@Override
	<RE> ImmutableSet.Builder<RE> immutableBuilder();

	@Override
	<RE> ImmutableSet.Builder<RE> immutableBuilder(int capacity);

	@Override
	default ImmutableSet<E> view()
	{
		return new SetView<>(this);
	}
}

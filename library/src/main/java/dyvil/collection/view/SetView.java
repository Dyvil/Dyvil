package dyvil.collection.view;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.iterator.ImmutableIterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Immutable
public class SetView<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = 816522991709785465L;

	protected final Set<E> set;

	public SetView(Set<E> collection)
	{
		this.set = collection;
	}

	@Override
	public int size()
	{
		return this.set.size();
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return this.set.isImmutable() ? this.set.iterator() : new ImmutableIterator<>(this.set.iterator());
	}

	@Override
	public void forEach(@NonNull Consumer<? super E> action)
	{
		this.set.forEach(action);
	}

	@NonNull
	@Override
	public ImmutableSet<E> added(E element)
	{
		return new SetView<>(this.set.added(element));
	}

	@NonNull
	@Override
	public ImmutableSet<E> removed(Object element)
	{
		return new SetView<>(this.set.removed(element));
	}

	@NonNull
	@Override
	public ImmutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		return new SetView<>(this.set.union(collection));
	}

	@NonNull
	@Override
	public ImmutableSet<E> difference(@NonNull Collection<?> collection)
	{
		return new SetView<>(this.set.difference(collection));
	}

	@NonNull
	@Override
	public ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		return new SetView<>(this.set.intersection(collection));
	}

	@NonNull
	@Override
	public ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		return new SetView<>(this.set.symmetricDifference(collection));
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return new SetView<>(this.set.mapped(mapper));
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		return new SetView<>(this.set.flatMapped(mapper));
	}

	@NonNull
	@Override
	public ImmutableSet<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		return new SetView<>(this.set.filtered(predicate));
	}

	@NonNull
	@Override
	public ImmutableSet<E> copy()
	{
		return new SetView<>(this.set.copy());
	}

	@Override
	public <RE> MutableSet<RE> emptyCopy()
	{
		return this.set.emptyCopy();
	}

	@NonNull
	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return this.set.emptyCopy(capacity);
	}

	@NonNull
	@Override
	public MutableSet<E> mutable()
	{
		return this.set.mutable();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return this.set.immutableBuilder();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return this.set.immutableBuilder(capacity);
	}

	@Override
	public java.util.@NonNull Set<E> toJava()
	{
		return this.set.isImmutable() ? this.set.toJava() : Collections.unmodifiableSet(this.set.toJava());
	}

	@NonNull
	@Override
	public String toString()
	{
		return "view " + this.set.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		return this.set.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return this.set.hashCode();
	}
}

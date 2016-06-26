package dyvil.collection.view;

import dyvil.annotation.Immutable;
import dyvil.collection.*;
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
	
	@Override
	public Iterator<E> iterator()
	{
		return this.set.isImmutable() ? this.set.iterator() : new ImmutableIterator<>(this.set.iterator());
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		this.set.forEach(action);
	}
	
	@Override
	public ImmutableSet<E> added(E element)
	{
		return new SetView<>(this.set.added(element));
	}
	
	@Override
	public ImmutableSet<E> removed(Object element)
	{
		return new SetView<>(this.set.removed(element));
	}

	@Override
	public ImmutableSet<E> union(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.union(collection));
	}

	@Override
	public ImmutableSet<E> difference(Collection<?> collection)
	{
		return new SetView<>(this.set.difference(collection));
	}
	
	@Override
	public ImmutableSet<E> intersection(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.intersection(collection));
	}
	
	@Override
	public ImmutableSet<E> symmetricDifference(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.symmetricDifference(collection));
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return new SetView<>(this.set.mapped(mapper));
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return new SetView<>(this.set.flatMapped(mapper));
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		return new SetView<>(this.set.filtered(condition));
	}
	
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

	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return this.set.emptyCopy(capacity);
	}
	
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
	public java.util.Set<E> toJava()
	{
		return this.set.isImmutable() ? this.set.toJava() : Collections.unmodifiableSet(this.set.toJava());
	}
	
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

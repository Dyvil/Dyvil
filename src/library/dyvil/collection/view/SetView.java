package dyvil.collection.view;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.iterator.ImmutableIterator;
import dyvil.annotation.Immutable;

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
	public ImmutableSet<E> $plus(E element)
	{
		return new SetView<>(this.set.$plus(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.$plus$plus(collection));
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		return new SetView<>(this.set.$minus(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		return new SetView<>(this.set.$minus$minus(collection));
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.$amp(collection));
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.$bar(collection));
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		return new SetView<>(this.set.$up(collection));
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

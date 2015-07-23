package dyvil.collection.view;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.iterator.ImmutableIterator;

public class SetView<E> implements ImmutableSet<E>
{
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
		return this.set.isImmutable() ? this.set.iterator() : new ImmutableIterator(this.set.iterator());
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
		this.set.forEach(action);
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		return new SetView(this.set.$plus(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		return new SetView(this.set.$plus$plus(collection));
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		return new SetView(this.set.$minus(element));
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
	{
		return new SetView(this.set.$minus$minus(collection));
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		return new SetView(this.set.$amp(collection));
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		return new SetView(this.set.$bar(collection));
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		return new SetView(this.set.$up(collection));
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return new SetView(this.set.mapped(mapper));
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return new SetView(this.set.flatMapped(mapper));
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		return new SetView(this.set.filtered(condition));
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return new SetView(this.set.copy());
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return this.set.mutable();
	}
}

package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.annotation.object;
import dyvil.array.ObjectArray;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.Collection;
import dyvil.lang.Set;
import dyvil.lang.literal.NilConvertible;

@NilConvertible
public @object class EmptySet<E> implements ImmutableSet<E>
{
	public static final EmptySet	instance	= new EmptySet();
	
	public static <E> EmptySet<E> apply()
	{
		return instance;
	}
	
	private EmptySet()
	{
	}
	
	@Override
	public int size()
	{
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return true;
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return EmptyIterator.apply();
	}
	
	@Override
	public Spliterator<E> spliterator()
	{
		return Spliterators.emptySpliterator();
	}
	
	@Override
	public void forEach(Consumer<? super E> action)
	{
	}
	
	@Override
	public boolean $qmark(Object element)
	{
		return false;
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
	{
		return new SingletonSet(element);
	}
	
	@Override
	public ImmutableSet<E> $minus(Object element)
	{
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<? extends E> collection)
	{
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
	{
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
	{
		return new ArraySet(collection);
	}
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
	{
		return new ArraySet(collection);
	}
	
	@Override
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		return (ImmutableSet<R>) this;
	}
	
	@Override
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		return (ImmutableSet<R>) this;
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
	{
		return this;
	}
	
	@Override
	public Object[] toArray()
	{
		return ObjectArray.EMPTY;
	}
	
	@Override
	public E[] toArray(Class<E> type)
	{
		return (E[]) ObjectArray.EMPTY;
	}
	
	@Override
	public void toArray(int index, Object[] store)
	{
	}
	
	@Override
	public ImmutableSet<E> copy()
	{
		return this;
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return MutableSet.apply();
	}
	
	@Override
	public String toString()
	{
		return "[]";
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Set.setEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Set.setHashCode(this);
	}
}

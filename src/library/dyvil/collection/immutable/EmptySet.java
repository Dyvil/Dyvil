package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation._internal.DyvilModifiers;
import dyvil.array.ObjectArray;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.literal.NilConvertible;
import dyvil.reflect.Modifiers;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class EmptySet<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = -6445525479912514756L;
	
	public static final EmptySet instance = new EmptySet();
	
	public static <E> EmptySet<E> apply()
	{
		return (EmptySet<E>) instance;
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
		return (Iterator<E>) EmptyIterator.instance;
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
	public boolean contains(Object element)
	{
		return false;
	}
	
	@Override
	public ImmutableSet<E> added(E element)
	{
		return new SingletonSet<>(element);
	}
	
	@Override
	public ImmutableSet<E> removed(Object element)
	{
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> difference(Collection<?> collection)
	{
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> intersection(Collection<? extends E> collection)
	{
		return this;
	}
	
	@Override
	public ImmutableSet<? extends E> union(Collection<? extends E> collection)
	{
		return ImmutableSet.from(collection);
	}
	
	@Override
	public ImmutableSet<? extends E> symmetricDifference(Collection<? extends E> collection)
	{
		return ImmutableSet.from(collection);
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
	public <RE> MutableSet<RE> emptyCopy()
	{
		return MutableSet.apply();
	}

	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return MutableSet.withCapacity(capacity);
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return MutableSet.apply();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return ImmutableSet.builder();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return ImmutableSet.builder(capacity);
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		return (java.util.Set<E>) Collections.EMPTY_SET;
	}
	
	@Override
	public String toString()
	{
		return Collection.EMPTY_STRING;
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
	
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}
	
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}

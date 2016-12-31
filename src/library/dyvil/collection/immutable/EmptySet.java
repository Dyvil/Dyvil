package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.array.ObjectArray;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;

import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromNil
@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class EmptySet<E> implements ImmutableSet<E>
{
	private static final long serialVersionUID = -6445525479912514756L;

	public static final EmptySet instance = new EmptySet();

	@NonNull
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

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return (Iterator<E>) EmptyIterator.instance;
	}

	@NonNull
	@Override
	public Spliterator<E> spliterator()
	{
		return Spliterators.emptySpliterator();
	}

	@Override
	public void forEach(@NonNull Consumer<? super E> action)
	{
	}

	@Override
	public boolean contains(Object element)
	{
		return false;
	}

	@NonNull
	@Override
	public ImmutableSet<E> added(E element)
	{
		return new SingletonSet<>(element);
	}

	@NonNull
	@Override
	public ImmutableSet<E> removed(Object element)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableSet<E> difference(@NonNull Collection<?> collection)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		return ImmutableSet.from(collection);
	}

	@NonNull
	@Override
	public ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		return ImmutableSet.from(collection);
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return (ImmutableSet<R>) this;
	}

	@NonNull
	@Override
	public <R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		return (ImmutableSet<R>) this;
	}

	@NonNull
	@Override
	public ImmutableSet<E> filtered(@NonNull Predicate<? super E> condition)
	{
		return this;
	}

	@NonNull
	@Override
	public Object[] toArray()
	{
		return ObjectArray.EMPTY;
	}

	@NonNull
	@Override
	public E[] toArray(Class<E> type)
	{
		return (E[]) ObjectArray.EMPTY;
	}

	@Override
	public void toArray(int index, Object[] store)
	{
	}

	@NonNull
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
	public java.util.@NonNull Set<E> toJava()
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

	@NonNull
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}

	@NonNull
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}

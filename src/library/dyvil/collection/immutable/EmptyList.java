package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.array.ObjectArray;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.List;
import dyvil.collection.MutableList;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.reflect.Modifiers;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class EmptyList<E> implements ImmutableList<E>
{
	private static final long serialVersionUID = -6059901529322971155L;

	public static final EmptyList instance = new EmptyList();

	@NonNull
	public static <E> EmptyList<E> apply()
	{
		return (EmptyList<E>) instance;
	}

	private EmptyList()
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
	public Iterator<E> reverseIterator()
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
	public <R> R foldLeft(R initialValue, @NonNull BiFunction<? super R, ? super E, ? extends R> reducer)
	{
		return initialValue;
	}

	@Override
	public <R> R foldRight(R initialValue, @NonNull BiFunction<? super E, ? super R, ? extends R> reducer)
	{
		return initialValue;
	}

	@Nullable
	@Override
	public E reduceLeft(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return null;
	}

	@Nullable
	@Override
	public E reduceRight(@NonNull BiFunction<? super E, ? super E, ? extends E> reducer)
	{
		return null;
	}

	@Override
	public boolean contains(Object element)
	{
		return false;
	}

	@NonNull
	@Override
	public E subscript(int index)
	{
		throw new IndexOutOfBoundsException("Empty List.apply()");
	}

	@Override
	public E get(int index)
	{
		return null;
	}

	@NonNull
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		if (startIndex > 0 || length > 0)
		{
			throw new IndexOutOfBoundsException("Empty List Slice out of range");
		}
		return this;
	}

	@Override
	public int indexOf(Object element)
	{
		return -1;
	}

	@Override
	public int lastIndexOf(Object element)
	{
		return -1;
	}

	@NonNull
	@Override
	public ImmutableList<E> added(E element)
	{
		return new SingletonList<>(element);
	}

	@NonNull
	@Override
	public ImmutableList<E> union(@NonNull Collection<? extends E> collection)
	{
		return ImmutableList.from(collection);
	}

	@NonNull
	@Override
	public ImmutableList<E> removed(Object element)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> difference(@NonNull Collection<?> collection)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> intersection(@NonNull Collection<? extends E> collection)
	{
		return this;
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		return (ImmutableList<R>) this;
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		return (ImmutableList<R>) this;
	}

	@NonNull
	@Override
	public ImmutableList<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> reversed()
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted()
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct()
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		return this;
	}

	@Override
	public Object @NonNull [] toArray()
	{
		return ObjectArray.EMPTY;
	}

	@Override
	public E @NonNull [] toArray(@NonNull Class<E> type)
	{
		return ObjectArray.ofType(0, type);
	}

	@Override
	public void toArray(int index, Object @NonNull [] store)
	{
	}

	@NonNull
	@Override
	public ImmutableList<E> copy()
	{
		return this;
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy()
	{
		return MutableList.apply();
	}

	@NonNull
	@Override
	public <RE> MutableList<RE> emptyCopy(int capacity)
	{
		return MutableList.withCapacity(capacity);
	}

	@NonNull
	@Override
	public MutableList<E> mutable()
	{
		return MutableList.apply();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder()
	{
		return AppendList.builder();
	}

	@Override
	public <RE> Builder<RE> immutableBuilder(int capacity)
	{
		return ImmutableList.builder(capacity);
	}

	@Override
	public java.util.@NonNull List<E> toJava()
	{
		return (java.util.List<E>) Collections.EMPTY_LIST;
	}

	@NonNull
	@Override
	public String toString()
	{
		return Collection.EMPTY_STRING;
	}

	@Override
	public boolean equals(Object obj)
	{
		return dyvil.collection.List.listEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return List.listHashCode(this);
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

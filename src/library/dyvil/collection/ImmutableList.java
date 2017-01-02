package dyvil.collection;

import dyvil.annotation.Immutable;
import dyvil.annotation.Mutating;
import dyvil.annotation.internal.Covariant;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.immutable.AppendList;
import dyvil.collection.immutable.ArrayList;
import dyvil.collection.immutable.EmptyList;
import dyvil.collection.immutable.SingletonList;
import dyvil.lang.LiteralConvertible;
import dyvil.ref.ObjectRef;
import dyvil.util.ImmutableException;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
@Immutable
public interface ImmutableList<@Covariant E> extends List<E>, ImmutableCollection<E>
{
	interface Builder<E> extends ImmutableCollection.Builder<E>
	{
		@Override
		ImmutableList<E> build();
	}

	@NonNull
	static <E> ImmutableList<E> apply()
	{
		return EmptyList.apply();
	}

	@NonNull
	static <E> ImmutableList<E> apply(E element)
	{
		return SingletonList.apply(element);
	}

	@NonNull
	static <E> ImmutableList<E> apply(E e1, E e2)
	{
		return ArrayList.apply(e1, e2);
	}

	@NonNull
	static <E> ImmutableList<E> apply(E e1, E e2, E e3)
	{
		return ArrayList.apply(e1, e2, e3);
	}

	@NonNull
	@SafeVarargs
	static <E> ImmutableList<E> apply(E... elements)
	{
		return ArrayList.apply(elements);
	}

	@NonNull
	static <E> ImmutableList<E> from(E @NonNull [] array)
	{
		return ArrayList.from(array);
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@NonNull
	static <E> ImmutableList<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return ArrayList.from(iterable);
	}

	@NonNull
	static <E> ImmutableList<E> from(@NonNull Collection<? extends E> collection)
	{
		return ArrayList.from(collection);
	}

	@NonNull
	static <E> ImmutableList<E> repeat(int count, E repeatedValue)
	{
		E[] elements = (E[]) new Object[count];
		for (int i = 0; i < count; i++)
		{
			elements[i] = repeatedValue;
		}
		return new ArrayList<>(elements, count, true);
	}

	@SuppressWarnings("LambdaUnfriendlyMethodOverload")
	@NonNull
	static <E> ImmutableList<E> generate(int count, @NonNull IntFunction<E> generator)
	{
		E[] elements = (E[]) new Object[count];
		for (int i = 0; i < count; i++)
		{
			elements[i] = generator.apply(i);
		}
		return new ArrayList<>(elements, count, true);
	}

	@NonNull
	@SafeVarargs
	static <E> ImmutableList<E> linked(E... elements)
	{
		return AppendList.apply(elements);
	}

	@NonNull
	static <E> Builder<E> builder()
	{
		return ArrayList.builder();
	}

	@NonNull
	static <E> Builder<E> builder(int capacity)
	{
		return ArrayList.builder(capacity);
	}

	@NonNull
	static <E> Builder<E> linkedBuilder()
	{
		return AppendList.builder();
	}

	// Accessors

	@Override
	int size();

	@NonNull
	@Override
	Iterator<E> iterator();

	@NonNull
	@Override
	Iterator<E> reverseIterator();

	@NonNull
	@Override
	default Spliterator<E> spliterator()
	{
		return Spliterators.spliterator(this.iterator(), this.size(), Spliterator.SIZED | Spliterator.IMMUTABLE);
	}

	@Override
	E get(int index);

	// Non-mutating Operations

	@NonNull
	@Override
	ImmutableList<E> subList(int startIndex, int length);

	@NonNull
	@Override
	ImmutableList<E> added(E element);

	@NonNull
	@Override
	ImmutableList<E> union(@NonNull Collection<? extends E> collection);

	@NonNull
	@Override
	ImmutableList<E> removed(Object element);

	@NonNull
	@Override
	ImmutableList<E> difference(@NonNull Collection<?> collection);

	@NonNull
	@Override
	ImmutableList<E> intersection(@NonNull Collection<? extends E> collection);

	@NonNull
	@Override
	<R> ImmutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper);

	@NonNull
	@Override
	<R> ImmutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper);

	@NonNull
	@Override
	ImmutableList<E> filtered(@NonNull Predicate<? super E> condition);

	@NonNull
	@Override
	ImmutableList<E> reversed();

	@NonNull
	@Override
	ImmutableList<E> sorted();

	@NonNull
	@Override
	ImmutableList<E> sorted(@NonNull Comparator<? super E> comparator);

	@NonNull
	@Override
	ImmutableList<E> distinct();

	@NonNull
	@Override
	ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator);

	// Mutating Operations

	@Override
	@Mutating
	default void clear()
	{
		throw new ImmutableException("clear() on Immutable List");
	}

	@Override
	@Mutating
	default void ensureCapacity(int minSize)
	{
	}

	@Override
	@Mutating
	default void subscript_$eq(int index, E element)
	{
		throw new ImmutableException("subscript() on Immutable List");
	}

	@NonNull
	@Override
	@Mutating
	default List<E> subscript(@NonNull Range<Integer> range)
	{
		throw new ImmutableException("subscript() on Immutable List");
	}

	@Override
	@Mutating
	default void subscript_$eq(@NonNull Range<Integer> range, E @NonNull [] elements)
	{
		throw new ImmutableException("subscript_=() on Immutable List");
	}

	@Override
	@Mutating
	default void subscript_$eq(@NonNull Range<Integer> range, @NonNull List<? extends E> elements)
	{
		throw new ImmutableException("subscript_=() on Immutable List");
	}

	@NonNull
	@Override
	@Mutating
	default ObjectRef<E> subscript_$amp(int index)
	{
		throw new ImmutableException("subscript_&() on Immutable List");
	}

	@NonNull
	@Override
	@Mutating
	default E set(int index, E element)
	{
		throw new ImmutableException("set() on Immutable List");
	}

	@NonNull
	@Override
	@Mutating
	default E setResizing(int index, E element)
	{
		throw new ImmutableException("setResizing() on Immutable List");
	}

	@Override
	@Mutating
	default void insert(int index, E element)
	{
		throw new ImmutableException("insert() on Immutable List");
	}

	@Override
	@Mutating
	default void insertResizing(int index, E element)
	{
		throw new ImmutableException("insertResizing() on Immutable List");
	}

	@Override
	@Mutating
	default void addElement(E element)
	{
		throw new ImmutableException("addElement() on Immutable List");
	}

	@Override
	@Mutating
	default boolean add(E element)
	{
		throw new ImmutableException("add() on Immutable List");
	}

	@Override
	@Mutating
	default boolean addAll(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("addAll() on Immutable List");
	}

	@Override
	@Mutating
	default boolean remove(Object element)
	{
		throw new ImmutableException("remove() on Immutable List");
	}

	@Override
	@Mutating
	default boolean removeFirst(Object element)
	{
		throw new ImmutableException("removeFirst() on Immutable List");
	}

	@Override
	@Mutating
	default boolean removeLast(Object element)
	{
		throw new ImmutableException("removeLast() on Immutable List");
	}

	@Override
	@Mutating
	default void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable List");
	}

	@Override
	@Mutating
	default boolean removeAll(@NonNull Collection<?> collection)
	{
		throw new ImmutableException("removeAll() on Immutable List");
	}

	@Override
	@Mutating
	default boolean retainAll(@NonNull Collection<? extends E> collection)
	{
		throw new ImmutableException("intersect() on Immutable List");
	}

	@Override
	@Mutating
	default void filter(@NonNull Predicate<? super E> condition)
	{
		throw new ImmutableException("filter() on Immutable List");
	}

	@Override
	@Mutating
	default void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		throw new ImmutableException("map() on Immutable List");
	}

	@Override
	@Mutating
	default void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		throw new ImmutableException("flatMap() on Immutable List");
	}

	@Override
	@Mutating
	default void reverse()
	{
		throw new ImmutableException("reverse() on Immutable List");
	}

	@Override
	@Mutating
	default void sort()
	{
		throw new ImmutableException("sort() on Immutable List");
	}

	@Override
	@Mutating
	default void sort(@NonNull Comparator<? super E> comparator)
	{
		throw new ImmutableException("sort() on Immutable List");
	}

	@Override
	@Mutating
	default void distinguish()
	{
		throw new ImmutableException("distinguish() on Immutable List");
	}

	@Override
	@Mutating
	default void distinguish(@NonNull Comparator<? super E> comparator)
	{
		throw new ImmutableException("disinguish() on Immutable List");
	}

	// Searching

	@Override
	int indexOf(Object element);

	@Override
	int lastIndexOf(Object element);

	// Copying and Views

	@NonNull
	@Override
	ImmutableList<E> copy();

	@NonNull
	@Override
	<RE> MutableList<RE> emptyCopy();

	@NonNull
	@Override
	<RE> MutableList<RE> emptyCopy(int capacity);

	@NonNull
	@Override
	MutableList<E> mutable();

	@NonNull
	@Override
	default MutableList<E> mutableCopy()
	{
		return this.mutable();
	}

	@NonNull
	@Override
	default ImmutableList<E> immutable()
	{
		return this;
	}

	@NonNull
	@Override
	default ImmutableList<E> immutableCopy()
	{
		return this.copy();
	}

	@Override
	<RE> Builder<RE> immutableBuilder();

	@Override
	<RE> Builder<RE> immutableBuilder(int capacity);

	@NonNull
	@Override
	default ImmutableList<E> view()
	{
		return this;
	}
}

package dyvil.collection;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.mutable.ArrayList;
import dyvil.collection.view.ListView;
import dyvil.lang.LiteralConvertible;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
public interface MutableList<E> extends List<E>, MutableCollection<E>
{
	@NonNull
	static <E> MutableList<E> apply()
	{
		return new ArrayList<>();
	}

	@NonNull
	static <E> MutableList<E> withCapacity(int capacity)
	{
		return new ArrayList<>(capacity);
	}

	@NonNull
	static <E> MutableList<E> apply(E element)
	{
		return ArrayList.apply(element);
	}

	@NonNull
	static <E> MutableList<E> apply(E e1, E e2)
	{
		return ArrayList.apply(e1, e2);
	}

	@NonNull
	static <E> MutableList<E> apply(E e1, E e2, E e3)
	{
		return ArrayList.apply(e1, e2, e3);
	}

	@NonNull
	@SafeVarargs
	static <E> MutableList<E> apply(E... elements)
	{
		return ArrayList.apply(elements);
	}

	@NonNull
	static <E> MutableList<E> from(E @NonNull [] array)
	{
		return ArrayList.from(array);
	}

	@NonNull
	static <E> MutableList<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return ArrayList.from(iterable);
	}

	@NonNull
	static <E> MutableList<E> from(@NonNull Collection<? extends E> collection)
	{
		return ArrayList.from(collection);
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

	@Override
	E get(int index);

	// Non-mutating Operations

	@NonNull
	@Override
	default MutableList<E> subList(int startIndex, int length)
	{
		MutableList<E> result = this.emptyCopy(length);
		for (int i = 0; i < length; i++)
		{
			result.add(this.get(startIndex + i));
		}
		return result;
	}

	@NonNull
	@Override
	default MutableList<E> added(E element)
	{
		MutableList<E> copy = this.copy();
		copy.addElement(element);
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> removed(@Nullable Object element)
	{
		MutableList<E> copy = this.emptyCopy();
		if (element == null)
		{
			for (E e : this)
			{
				if (e != null)
				{
					copy.add(e);
				}
			}
		}
		else
		{
			for (E e : this)
			{
				if (!element.equals(e))
				{
					copy.add(e);
				}
			}
		}
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> difference(@NonNull Collection<?> collection)
	{
		MutableList<E> copy = this.emptyCopy();
		for (E e : this)
		{
			if (!collection.contains(e))
			{
				copy.add(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> union(@NonNull Collection<? extends E> collection)
	{
		MutableList<E> copy = this.copy(this.size() + collection.size());
		copy.addAll(collection);
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> intersection(@NonNull Collection<? extends E> collection)
	{
		MutableList<E> copy = this.emptyCopy(Math.min(this.size(), collection.size()));
		for (E e : this)
		{
			if (collection.contains(e))
			{
				copy.add(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	default <R> MutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		MutableList<R> copy = (MutableList<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}

	@NonNull
	@Override
	default <R> MutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		MutableList<R> copy = this.emptyCopy(this.size() << 2);
		for (E e : this)
		{
			for (R r : mapper.apply(e))
			{
				copy.add(r);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		MutableList<E> copy = this.emptyCopy();
		for (E e : this)
		{
			if (predicate.test(e))
			{
				copy.add(e);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	default List<E> reversed()
	{
		MutableList<E> result = this.emptyCopy(this.size());
		for (Iterator<E> iterator = this.reverseIterator(); iterator.hasNext(); )
		{
			E element = iterator.next();
			result.add(element);
		}
		return result;
	}

	@NonNull
	@Override
	default MutableList<E> sorted()
	{
		MutableList<E> copy = this.copy();
		copy.sort();
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		MutableList<E> copy = this.copy();
		copy.sort(comparator);
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> distinct()
	{
		MutableList<E> copy = this.copy();
		copy.distinguish();
		return copy;
	}

	@NonNull
	@Override
	default MutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		MutableList<E> copy = this.copy();
		copy.distinct(comparator);
		return copy;
	}

	// Mutating Operations

	@Override
	void addElement(E element);

	@Nullable
	@Override
	E set(int index, E element);

	@Nullable
	@Override
	E setResizing(int index, E element);

	@Override
	void insert(int index, E element);

	@Override
	default boolean add(E element)
	{
		// Duplicate override because of conflicting default methods
		this.addElement(element);
		return true;
	}

	@Override
	void removeAt(int index);

	@Override
	boolean remove(Object element);

	@Override
	void clear();

	@Override
	void map(@NonNull Function<? super E, ? extends E> mapper);

	@Override
	void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper);

	@Override
	void reverse();

	@Override
	void sort();

	@Override
	void sort(@NonNull Comparator<? super E> comparator);

	@Override
	void distinguish();

	@Override
	void distinguish(@NonNull Comparator<? super E> comparator);

	// Search Operations

	@Override
	int indexOf(Object element);

	@Override
	int lastIndexOf(Object element);

	// Copying

	@NonNull
	@Override
	MutableList<E> copy();

	@NonNull
	default MutableList<E> copy(int capacity)
	{
		return this.copy();
	}

	@NonNull
	@Override
	default MutableList<E> mutable()
	{
		return this;
	}

	@NonNull
	@Override
	default MutableList<E> mutableCopy()
	{
		return this.copy();
	}

	@NonNull
	@Override
	<R> MutableList<R> emptyCopy();

	@NonNull
	@Override
	default <R> MutableList<R> emptyCopy(int newCapacity)
	{
		return this.emptyCopy();
	}

	@NonNull
	@Override
	ImmutableList<E> immutable();

	@NonNull
	@Override
	default ImmutableList<E> immutableCopy()
	{
		return this.immutable();
	}

	@Override
	<RE> ImmutableList.Builder<RE> immutableBuilder();

	@Override
	<RE> ImmutableList.Builder<RE> immutableBuilder(int capacity);

	@NonNull
	@Override
	default ImmutableList<E> view()
	{
		return new ListView<>(this);
	}
}

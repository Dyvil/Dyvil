package dyvil.collection;

import dyvil.collection.mutable.ArrayList;
import dyvil.collection.view.ListView;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

@NilConvertible
@ArrayConvertible
public interface MutableList<E> extends List<E>, MutableCollection<E>
{
	static <E> MutableList<E> apply()
	{
		return new ArrayList<>();
	}

	static <E> MutableList<E> withCapacity(int capacity)
	{
		return new ArrayList<>(capacity);
	}
	
	static <E> MutableList<E> apply(E element)
	{
		return ArrayList.apply(element);
	}
	
	static <E> MutableList<E> apply(E e1, E e2)
	{
		return ArrayList.apply(e1, e2);
	}
	
	static <E> MutableList<E> apply(E e1, E e2, E e3)
	{
		return ArrayList.apply(e1, e2, e3);
	}
	
	@SafeVarargs
	static <E> MutableList<E> apply(E... elements)
	{
		return ArrayList.apply(elements);
	}
	
	static <E> MutableList<E> from(E[] array)
	{
		return ArrayList.from(array);
	}

	static <E> MutableList<E> from(Iterable<? extends E> iterable)
	{
		return ArrayList.from(iterable);
	}

	static <E> MutableList<E> from(Collection<? extends E> collection)
	{
		return ArrayList.from(collection);
	}
	
	// Accessors
	
	@Override
	int size();
	
	@Override
	Iterator<E> iterator();
	
	@Override
	Iterator<E> reverseIterator();
	
	@Override
	E get(int index);
	
	// Non-mutating Operations
	
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
	
	@Override
	default MutableList<E> added(E element)
	{
		MutableList<E> copy = this.copy();
		copy.addElement(element);
		return copy;
	}
	
	@Override
	default MutableList<E> removed(Object element)
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
	
	@Override
	default MutableList<E> difference(Collection<?> collection)
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
	
	@Override
	default MutableList<E> union(Collection<? extends E> collection)
	{
		MutableList<E> copy = this.copy(this.size() + collection.size());
		copy.addAll(collection);
		return copy;
	}
	
	@Override
	default MutableList<E> intersection(Collection<? extends E> collection)
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

	@Override
	@SuppressWarnings("unchecked")
	default <R> MutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableList<R> copy = (MutableList<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}
	
	@Override
	default <R> MutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
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
	
	@Override
	default MutableList<E> filtered(Predicate<? super E> condition)
	{
		MutableList<E> copy = this.emptyCopy();
		for (E e : this)
		{
			if (condition.test(e))
			{
				copy.add(e);
			}
		}
		return copy;
	}

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
	
	@Override
	default MutableList<E> sorted()
	{
		MutableList<E> copy = this.copy();
		copy.sort();
		return copy;
	}
	
	@Override
	default MutableList<E> sorted(Comparator<? super E> comparator)
	{
		MutableList<E> copy = this.copy();
		copy.sort(comparator);
		return copy;
	}
	
	@Override
	default MutableList<E> distinct()
	{
		MutableList<E> copy = this.copy();
		copy.distinguish();
		return copy;
	}
	
	@Override
	default MutableList<E> distinct(Comparator<? super E> comparator)
	{
		MutableList<E> copy = this.copy();
		copy.distinct(comparator);
		return copy;
	}
	
	// Mutating Operations
	
	@Override
	void addElement(E element);

	@Override
	E set(int index, E element);

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
	void map(Function<? super E, ? extends E> mapper);
	
	@Override
	void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	void reverse();
	
	@Override
	void sort();
	
	@Override
	void sort(Comparator<? super E> comparator);
	
	@Override
	void distinguish();
	
	@Override
	void distinguish(Comparator<? super E> comparator);
	
	// Search Operations
	
	@Override
	int indexOf(Object element);
	
	@Override
	int lastIndexOf(Object element);
	
	// Copying
	
	@Override
	MutableList<E> copy();

	default MutableList<E> copy(int capacity)
	{
		return this.copy();
	}
	
	@Override
	default MutableList<E> mutable()
	{
		return this;
	}
	
	@Override
	default MutableList<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	<R> MutableList<R> emptyCopy();
	
	@Override
	default <R> MutableList<R> emptyCopy(int newCapacity)
	{
		return this.emptyCopy();
	}
	
	@Override
	ImmutableList<E> immutable();
	
	@Override
	default ImmutableList<E> immutableCopy()
	{
		return this.immutable();
	}

	@Override
	<RE> ImmutableList.Builder<RE> immutableBuilder();

	@Override
	<RE> ImmutableList.Builder<RE> immutableBuilder(int capacity);

	@Override
	default ImmutableList<E> view()
	{
		return new ListView<>(this);
	}
}

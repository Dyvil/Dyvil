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
	
	static <E> MutableList<E> apply(E element)
	{
		return new ArrayList<>((E[]) new Object[] { element }, 1, true);
	}
	
	static <E> MutableList<E> apply(E e1, E e2)
	{
		return new ArrayList<>((E[]) new Object[] { e1, e2 }, 2, true);
	}
	
	static <E> MutableList<E> apply(E e1, E e2, E e3)
	{
		return new ArrayList<>((E[]) new Object[] { e1, e2, e3 }, 3, true);
	}
	
	@SafeVarargs
	static <E> MutableList<E> apply(E... elements)
	{
		return new ArrayList<>(elements);
	}
	
	static <E> MutableList<E> fromArray(E[] elements)
	{
		return new ArrayList<>(elements, true);
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
	MutableList<E> subList(int startIndex, int length);
	
	default MutableList<E> withCapacity(int newCapacity)
	{
		return this.copy();
	}
	
	@Override
	default MutableList<E> $plus(E element)
	{
		MutableList<E> copy = this.copy();
		copy.$plus$eq(element);
		return copy;
	}
	
	@Override
	default MutableList<E> $minus(Object element)
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
	default MutableList<? extends E> $minus$minus(Collection<?> collection)
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
	default MutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		MutableList<E> copy = this.withCapacity(this.size() + collection.size());
		copy.$plus$plus$eq(collection);
		return copy;
	}
	
	@Override
	default MutableList<? extends E> $amp(Collection<? extends E> collection)
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
	
	/*
	 * Copying + in-place reversing the list is generally really slow, so force
	 * implementors to implement this method.
	 */
	@Override
	List<E> reversed();
	
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
	void $plus$eq(E element);

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
		this.$plus$eq(element);
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
	default ImmutableList<E> view()
	{
		return new ListView<>(this);
	}
}

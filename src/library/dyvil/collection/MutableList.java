package dyvil.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.mutable.ArrayList;

@NilConvertible
@ArrayConvertible
public interface MutableList<E> extends List<E>, MutableCollection<E>
{
	public static <E> MutableList<E> apply()
	{
		return new ArrayList();
	}
	
	public static <E> MutableList<E> apply(E element)
	{
		return new ArrayList(new Object[] { element }, 1, true);
	}
	
	public static <E> MutableList<E> apply(E e1, E e2)
	{
		return new ArrayList(new Object[] { e1, e2 }, 2, true);
	}
	
	public static <E> MutableList<E> apply(E e1, E e2, E e3)
	{
		return new ArrayList(new Object[] { e1, e2, e3 }, 3, true);
	}
	
	public static <E> MutableList<E> apply(E... elements)
	{
		return new ArrayList(elements, true);
	}
	
	// Accessors
	
	@Override
	public int size();
	
	@Override
	public Iterator<E> iterator();
	
	@Override
	public E subscript(int index);
	
	@Override
	public E get(int index);
	
	// Non-mutating Operations
	
	@Override
	public MutableList<E> subList(int startIndex, int length);
	
	public MutableList<E> resized(int newSize);
	
	public default MutableList<E> withCapacity(int newCapacity)
	{
		return this;
	}
	
	@Override
	public default MutableList<E> $plus(E element)
	{
		MutableList<E> copy = this.copy();
		copy.$plus$eq(element);
		return copy;
	}
	
	@Override
	public default MutableList<E> $minus(Object element)
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
	public default MutableList<? extends E> $minus$minus(Collection<?> collection)
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
	public default MutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		MutableList<E> copy = this.withCapacity(this.size() + collection.size());
		copy.$plus$plus$eq(collection);
		return copy;
	}
	
	@Override
	public default MutableList<? extends E> $amp(Collection<? extends E> collection)
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
	public default <R> MutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		MutableList<R> copy = (MutableList<R>) this.copy();
		copy.map((Function) mapper);
		return copy;
	}
	
	@Override
	public default <R> MutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
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
	public default MutableList<E> filtered(Predicate<? super E> condition)
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
	public default MutableList<E> sorted()
	{
		MutableList<E> copy = this.copy();
		copy.sort();
		return copy;
	}
	
	@Override
	public default MutableList<E> sorted(Comparator<? super E> comparator)
	{
		MutableList<E> copy = this.copy();
		copy.sort(comparator);
		return copy;
	}
	
	@Override
	public default MutableList<E> distinct()
	{
		MutableList<E> copy = this.copy();
		copy.distinguish();
		return copy;
	}
	
	@Override
	public default MutableList<E> distinct(Comparator<? super E> comparator)
	{
		MutableList<E> copy = this.copy();
		copy.distinct(comparator);
		return copy;
	}
	
	// Mutating Operations
	
	@Override
	public void $plus$eq(E element);
	
	@Override
	public void resize(int newLength);
	
	@Override
	public void subscript_$eq(int index, E element);
	
	@Override
	public E set(int index, E element);
	
	@Override
	public E add(int index, E element);
	
	@Override
	public void removeAt(int index);
	
	@Override
	public boolean remove(Object element);
	
	@Override
	public void clear();
	
	@Override
	public void map(Function<? super E, ? extends E> mapper);
	
	@Override
	public void flatMap(Function<? super E, ? extends Iterable<? extends E>> mapper);
	
	@Override
	public void sort();
	
	@Override
	public void sort(Comparator<? super E> comparator);
	
	@Override
	public void distinguish();
	
	@Override
	public void distinguish(Comparator<? super E> comparator);
	
	// Search Operations
	
	@Override
	public int indexOf(Object element);
	
	@Override
	public int lastIndexOf(Object element);
	
	// Copying
	
	@Override
	public MutableList<E> copy();
	
	@Override
	public default MutableList<E> mutable()
	{
		return this;
	}
	
	@Override
	public default MutableList<E> mutableCopy()
	{
		return this.copy();
	}
	
	@Override
	public <R> MutableList<R> emptyCopy();
	
	public default <R> MutableList<R> emptyCopy(int newCapacity)
	{
		return this.emptyCopy();
	}
	
	@Override
	public ImmutableList<E> immutable();
	
	@Override
	public default ImmutableList<E> immutableCopy()
	{
		return this.immutable();
	}
}

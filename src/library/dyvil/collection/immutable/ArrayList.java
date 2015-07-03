package dyvil.collection.immutable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

import dyvil.lang.Collection;
import dyvil.lang.Set;
import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.ImmutableList;
import dyvil.collection.MutableList;
import dyvil.collection.impl.AbstractArrayList;
import dyvil.collection.iterator.ArrayIterator;

@ArrayConvertible
public class ArrayList<E> extends AbstractArrayList<E> implements ImmutableList<E>
{
	public static <E> ArrayList<E> apply(E... elements)
	{
		return new ArrayList(elements, true);
	}
	
	public ArrayList()
	{
		super();
	}
	
	public ArrayList(int size)
	{
		super(size);
	}
	
	public ArrayList(Object... elements)
	{
		super(elements);
	}
	
	public ArrayList(Object[] elements, boolean trusted)
	{
		super(elements, trusted);
	}
	
	public ArrayList(Object[] elements, int size)
	{
		super(elements, size);
	}
	
	public ArrayList(Object[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}
	
	public ArrayList(Collection<E> collection)
	{
		super(collection);
	}
	
	@Override
	public Iterator<E> iterator()
	{
		return new ArrayIterator(this.elements, this.size);
	}
	
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		this.rangeCheck(startIndex);
		if (startIndex + length >= this.size)
		{
			throw new IndexOutOfBoundsException("Array Length out of Bounds: " + length);
		}
		
		Object[] array = new Object[length];
		System.arraycopy(this.elements, startIndex, array, 0, length);
		return new ArrayList(array, length, true);
	}
	
	@Override
	public ImmutableList<E> $plus(E element)
	{
		Object[] array = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		array[this.size] = element;
		return new ArrayList(array, this.size + 1, true);
	}
	
	@Override
	public ImmutableList<? extends E> $plus$plus(Collection<? extends E> collection)
	{
		int len = collection.size();
		Object[] array = new Object[this.size + len];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		
		Object[] array1 = collection.toArray();
		System.arraycopy(array1, 0, array, this.size, len);
		return new ArrayList(array, this.size + len, true);
	}
	
	@Override
	public ImmutableList<E> $minus(Object element)
	{
		int index = this.indexOf(element);
		if (index < 0)
		{
			return this;
		}
		
		Object[] array = new Object[this.size - 1];
		if (index > 0)
		{
			// copy the first part before the index
			System.arraycopy(this.elements, 0, array, 0, index);
		}
		if (index < this.size)
		{
			// copy the second part after the index
			System.arraycopy(this.elements, index + 1, array, index, this.size - index - 1);
		}
		return new ArrayList(array, this.size - 1, true);
	}
	
	@Override
	public ImmutableList<? extends E> $minus$minus(Collection<?> collection)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (!collection.contains(e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList(array, index, true);
	}
	
	@Override
	public ImmutableList<? extends E> $amp(Collection<? extends E> collection)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (collection.contains(e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList(array, index, true);
	}
	
	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			array[i] = mapper.apply((E) this.elements[i]);
		}
		return new ArrayList(array, this.size, true);
	}
	
	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		dyvil.collection.mutable.ArrayList<R> list = new dyvil.collection.mutable.ArrayList(this.size << 2);
		for (int i = 0; i < this.size; i++)
		{
			for (R r : mapper.apply((E) this.elements[i]))
			{
				list.$plus$eq(r);
			}
		}
		return list.immutable();
	}
	
	@Override
	public ImmutableList<E> filtered(Predicate<? super E> condition)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (condition.test((E) e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList(array, index, true);
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort(array, 0, this.size);
		return new SortedArrayList(array, this.size, true);
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort((E[]) array, 0, this.size, comparator);
		return new SortedArrayList(array, this.size, true);
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinct(array, this.size);
		return new ArrayList(array, size, true);
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinct((E[]) array, this.size, comparator);
		return new SortedArrayList(array, size, true);
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return new ArrayList(this.elements, this.size, true);
	}
	
	@Override
	public MutableList<E> mutable()
	{
		return new dyvil.collection.mutable.ArrayList(this.elements, this.size);
	}
}

package dyvil.collection.immutable;

import java.util.Comparator;

import dyvil.collection.ImmutableList;
import dyvil.lang.Collection;
import dyvil.lang.Set;

public class SortedArrayList<E> extends ArrayList<E>
{
	public SortedArrayList(E[] elements)
	{
		super(elements);
	}
	
	public SortedArrayList(E[] elements, int size)
	{
		super(elements, size);
	}
	
	public SortedArrayList(E[] elements, boolean trusted)
	{
		super(elements, trusted);
	}
	
	public SortedArrayList(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}
	
	public SortedArrayList(Collection<E> elements)
	{
		super(elements);
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		return new SortedArrayList(this.elements, this.size, true);
	}
	
	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		return new SortedArrayList(this.elements, this.size, true);
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinctSorted(array, this.size);
		return new SortedArrayList(array, size, true);
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinctSorted(array, this.size);
		return new SortedArrayList(array, size, true);
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return new SortedArrayList(this.elements, this.size, true);
	}
}

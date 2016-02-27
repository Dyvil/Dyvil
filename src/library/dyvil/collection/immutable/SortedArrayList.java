package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.Set;

import java.util.Arrays;
import java.util.Comparator;

@Immutable
public class SortedArrayList<E> extends ArrayList<E>
{
	private static final long serialVersionUID = -5735346326799929699L;
	
	protected Comparator<? super E> comparator;
	
	public SortedArrayList(E[] elements, Comparator<? super E> comparator)
	{
		super(elements);
		this.useComparator(comparator);
	}
	
	public SortedArrayList(E[] elements, int size, Comparator<? super E> comparator)
	{
		super(elements, size);
		this.useComparator(comparator);
	}
	
	public SortedArrayList(E[] elements, boolean trusted, Comparator<? super E> comparator)
	{
		super(elements, trusted);
		this.comparator = comparator;
	}
	
	public SortedArrayList(E[] elements, int size, boolean trusted, Comparator<? super E> comparator)
	{
		super(elements, size, trusted);
		this.comparator = comparator;
	}
	
	public SortedArrayList(Collection<E> elements, Comparator<? super E> comparator)
	{
		super(elements);
		this.useComparator(comparator);
	}
	
	private void useComparator(Comparator<? super E> comparator)
	{
		if (comparator == null)
		{
			Arrays.sort(this.elements, 0, this.size);
			return;
		}
		
		this.comparator = comparator;
		Arrays.sort((E[]) this.elements, 0, this.size, comparator);
	}
	
	@Override
	public boolean isSorted()
	{
		return this.comparator == null || super.isSorted();
	}
	
	@Override
	public boolean isSorted(Comparator<? super E> comparator)
	{
		return comparator == this.comparator || super.isSorted(comparator);
	}
	
	@Override
	public ImmutableList<E> sorted()
	{
		if (this.comparator == null)
		{
			return this;
		}
		
		return super.sorted();
	}
	
	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = this.comparator != null ?
				Set.sortDistinct((E[]) array, this.size, this.comparator) :
				Set.distinctSorted(array, this.size);
		return new SortedArrayList<>((E[]) array, size, true, this.comparator);
	}
	
	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = this.comparator != comparator ?
				Set.sortDistinct((E[]) array, this.size, comparator) :
				Set.distinctSorted(array, this.size);
		return new SortedArrayList<>((E[]) array, size, true, comparator);
	}
	
	@Override
	public ImmutableList<E> copy()
	{
		return new SortedArrayList<>((E[]) this.elements, this.size, true, this.comparator);
	}
}

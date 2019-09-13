package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableList;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractArrayList;

import java.util.Arrays;
import java.util.Comparator;

@Immutable
public class SortedArrayList<E> extends ArrayList<E>
{
	private static final long serialVersionUID = -5735346326799929699L;

	@Nullable
	protected Comparator<? super E> comparator;

	public SortedArrayList(E @NonNull [] elements, Comparator<? super E> comparator)
	{
		super(elements);
		this.useComparator(comparator);
	}

	public SortedArrayList(E @NonNull [] elements, int size, Comparator<? super E> comparator)
	{
		super(elements, size);
		this.useComparator(comparator);
	}

	public SortedArrayList(E @NonNull [] elements, boolean trusted, Comparator<? super E> comparator)
	{
		super(elements, trusted);
		this.comparator = comparator;
	}

	public SortedArrayList(E[] elements, int size, boolean trusted, Comparator<? super E> comparator)
	{
		super(elements, size, trusted);
		this.comparator = comparator;
	}

	public SortedArrayList(@NonNull Iterable<? extends E> iterable, Comparator<? super E> comparator)
	{
		super(iterable);
		this.useComparator(comparator);
	}

	public SortedArrayList(@NonNull Collection<? extends E> collection, Comparator<? super E> comparator)
	{
		super(collection);
		this.useComparator(comparator);
	}

	public SortedArrayList(@NonNull AbstractArrayList<? extends E> arrayList, Comparator<? super E> comparator)
	{
		super(arrayList);
		this.useComparator(comparator);
	}

	private void useComparator(@Nullable Comparator<? super E> comparator)
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
	public boolean isSorted(@NonNull Comparator<? super E> comparator)
	{
		return comparator == this.comparator || super.isSorted(comparator);
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted()
	{
		if (this.comparator == null)
		{
			return this;
		}

		return super.sorted();
	}

	@NonNull
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

	@NonNull
	@Override
	public ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = this.comparator != comparator ?
			           Set.sortDistinct((E[]) array, this.size, comparator) :
			           Set.distinctSorted(array, this.size);
		return new SortedArrayList<>((E[]) array, size, true, comparator);
	}

	@NonNull
	@Override
	public ImmutableList<E> copy()
	{
		return new SortedArrayList<>((E[]) this.elements, this.size, true, this.comparator);
	}
}

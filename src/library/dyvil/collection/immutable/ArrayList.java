package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayList;
import dyvil.lang.LiteralConvertible;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
@Immutable
public class ArrayList<E> extends AbstractArrayList<E> implements ImmutableList<E>
{
	public static class Builder<E> implements ImmutableList.Builder<E>
	{
		private ArrayList<E> list;

		public Builder()
		{
			this.list = new ArrayList<>();
		}

		public Builder(int capacity)
		{
			this.list = new ArrayList<>(capacity);
		}

		@Override
		public void add(E element)
		{
			if (this.list == null)
			{
				throw new IllegalStateException("Already built");
			}

			this.list.addInternal(element);
		}

		@Override
		public ArrayList<E> build()
		{
			final ArrayList<E> list = this.list;
			this.list = null;
			return list;
		}
	}

	private static final long serialVersionUID = 1107932890158514157L;

	// Factory Methods

	@SafeVarargs
	public static <E> ArrayList<E> apply(E... elements)
	{
		return new ArrayList<>(elements, true);
	}

	public static <E> ArrayList<E> from(E[] array)
	{
		return new ArrayList<>(array);
	}

	public static <E> ArrayList<E> from(Iterable<? extends E> iterable)
	{
		return new ArrayList<>(iterable);
	}

	public static <E> ArrayList<E> from(Collection<? extends E> collection)
	{
		return new ArrayList<>(collection);
	}

	public static <E> ArrayList<E> from(AbstractArrayList<? extends E> arrayList)
	{
		return new ArrayList<>(arrayList);
	}

	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	public static <E> Builder<E> builder(int capacity)
	{
		return new Builder<>(capacity);
	}

	// Constructors

	protected ArrayList()
	{
		super();
	}

	protected ArrayList(int capacity)
	{
		super(capacity);
	}

	public ArrayList(E[] elements)
	{
		super(elements);
	}

	public ArrayList(E[] elements, boolean trusted)
	{
		super(elements, elements.length, trusted);
	}

	public ArrayList(E[] elements, int size)
	{
		super(elements, size);
	}

	public ArrayList(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}

	public ArrayList(Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public ArrayList(Collection<? extends E> collection)
	{
		super(collection);
	}

	public ArrayList(AbstractArrayList<? extends E> arrayList)
	{
		super(arrayList);
	}

	// Implementation Methods

	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		List.rangeCheck(startIndex, this.size);
		List.rangeCheck(startIndex + length - 1, this.size);

		Object[] array = new Object[length];
		System.arraycopy(this.elements, startIndex, array, 0, length);
		return new ArrayList<>((E[]) array, length, true);
	}

	@Override
	public ImmutableList<E> added(E element)
	{
		Object[] array = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		array[this.size] = element;
		return new ArrayList<>((E[]) array, this.size + 1, true);
	}

	@Override
	public ImmutableList<E> union(Collection<? extends E> collection)
	{
		int len = collection.size();
		Object[] array = new Object[this.size + len];
		System.arraycopy(this.elements, 0, array, 0, this.size);

		Object[] array1 = collection.toArray();
		System.arraycopy(array1, 0, array, this.size, len);
		return new ArrayList<>((E[]) array, this.size + len, true);
	}

	@Override
	public ImmutableList<E> removed(Object element)
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
		return new ArrayList<>((E[]) array, this.size - 1, true);
	}

	@Override
	public ImmutableList<E> difference(Collection<?> collection)
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
		return new ArrayList<>((E[]) array, index, true);
	}

	@Override
	public ImmutableList<E> intersection(Collection<? extends E> collection)
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
		return new ArrayList<>((E[]) array, index, true);
	}

	@Override
	public <R> ImmutableList<R> mapped(Function<? super E, ? extends R> mapper)
	{
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			array[i] = mapper.apply((E) this.elements[i]);
		}
		return new ArrayList<>((R[]) array, this.size, true);
	}

	@Override
	public <R> ImmutableList<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		Builder<R> builder = new Builder<>(this.size << 2);
		for (int i = 0; i < this.size; i++)
		{
			for (R r : mapper.apply((E) this.elements[i]))
			{
				builder.add(r);
			}
		}
		return builder.build();
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
		return new ArrayList<>((E[]) array, index, true);
	}

	@Override
	public ImmutableList<E> reversed()
	{
		Object[] newArray = new Object[this.size];
		int index = this.size;
		for (Object o : this.elements)
		{
			newArray[--index] = o;
		}
		return new ArrayList<>((E[]) newArray, this.size, true);
	}

	@Override
	public ImmutableList<E> sorted()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort(array, 0, this.size);
		return new SortedArrayList<>((E[]) array, this.size, true, null);
	}

	@Override
	public ImmutableList<E> sorted(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort((E[]) array, 0, this.size, comparator);
		return new SortedArrayList<>((E[]) array, this.size, true, comparator);
	}

	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinct(array, this.size);
		return new ArrayList<>((E[]) array, size, true);
	}

	@Override
	public ImmutableList<E> distinct(Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.sortDistinct((E[]) array, this.size, comparator);
		return new SortedArrayList<>((E[]) array, size, true, comparator);
	}

	@Override
	public ImmutableList<E> copy()
	{
		return new ArrayList<>((E[]) this.elements, this.size, true);
	}

	@Override
	public MutableList<E> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public java.util.List<E> toJava()
	{
		return Collections.unmodifiableList(super.toJava());
	}
}

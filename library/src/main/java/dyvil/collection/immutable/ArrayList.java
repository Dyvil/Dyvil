package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
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

	@NonNull
	@SafeVarargs
	public static <E> ArrayList<E> apply(@NonNull E... elements)
	{
		return new ArrayList<>(elements, true);
	}

	@NonNull
	public static <E> ArrayList<E> from(E @NonNull [] array)
	{
		return new ArrayList<>(array);
	}

	@NonNull
	public static <E> ArrayList<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return new ArrayList<>(iterable);
	}

	@NonNull
	public static <E> ArrayList<E> from(@NonNull Collection<? extends E> collection)
	{
		return new ArrayList<>(collection);
	}

	@NonNull
	public static <E> ArrayList<E> from(@NonNull AbstractArrayList<? extends E> arrayList)
	{
		return new ArrayList<>(arrayList);
	}

	@NonNull
	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	@NonNull
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

	public ArrayList(E @NonNull [] elements)
	{
		super(elements);
	}

	public ArrayList(E @NonNull [] elements, boolean trusted)
	{
		super(elements, elements.length, trusted);
	}

	public ArrayList(E @NonNull [] elements, int size)
	{
		super(elements, size);
	}

	public ArrayList(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}

	public ArrayList(@NonNull Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public ArrayList(@NonNull Collection<? extends E> collection)
	{
		super(collection);
	}

	public ArrayList(@NonNull AbstractArrayList<? extends E> arrayList)
	{
		super(arrayList);
	}

	// Implementation Methods

	@NonNull
	@Override
	public ImmutableList<E> subList(int startIndex, int length)
	{
		List.rangeCheck(startIndex, this.size);
		List.rangeCheck(startIndex + length - 1, this.size);

		Object[] array = new Object[length];
		System.arraycopy(this.elements, startIndex, array, 0, length);
		return new ArrayList<>((E[]) array, length, true);
	}

	@NonNull
	@Override
	public ImmutableList<E> added(E element)
	{
		Object[] array = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		array[this.size] = element;
		return new ArrayList<>((E[]) array, this.size + 1, true);
	}

	@NonNull
	@Override
	public ImmutableList<E> union(@NonNull Collection<? extends E> collection)
	{
		int len = collection.size();
		Object[] array = new Object[this.size + len];
		System.arraycopy(this.elements, 0, array, 0, this.size);

		Object[] array1 = collection.toArray();
		System.arraycopy(array1, 0, array, this.size, len);
		return new ArrayList<>((E[]) array, this.size + len, true);
	}

	@NonNull
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

	@NonNull
	@Override
	public ImmutableList<E> difference(@NonNull Collection<?> collection)
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

	@NonNull
	@Override
	public ImmutableList<E> intersection(@NonNull Collection<? extends E> collection)
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

	@NonNull
	@Override
	public <R> ImmutableList<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			array[i] = mapper.apply((E) this.elements[i]);
		}
		return new ArrayList<>((R[]) array, this.size, true);
	}

	@NonNull
	@Override
	public <R> ImmutableList<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
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

	@NonNull
	@Override
	public ImmutableList<E> filtered(@NonNull Predicate<? super E> predicate)
	{
		int index = 0;
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (predicate.test((E) e))
			{
				array[index++] = e;
			}
		}
		return new ArrayList<>((E[]) array, index, true);
	}

	@NonNull
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

	@NonNull
	@Override
	public ImmutableList<E> sorted()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort(array, 0, this.size);
		return new SortedArrayList<>((E[]) array, this.size, true, null);
	}

	@NonNull
	@Override
	public ImmutableList<E> sorted(@NonNull Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		Arrays.sort((E[]) array, 0, this.size, comparator);
		return new SortedArrayList<>((E[]) array, this.size, true, comparator);
	}

	@NonNull
	@Override
	public ImmutableList<E> distinct()
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.distinct(array, this.size);
		return new ArrayList<>((E[]) array, size, true);
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public ImmutableList<E> distinct(@NonNull Comparator<? super E> comparator)
	{
		Object[] array = new Object[this.size];
		System.arraycopy(this.elements, 0, array, 0, this.size);
		int size = Set.sortDistinct((E[]) array, this.size, comparator);
		return new SortedArrayList<>((E[]) array, size, true, comparator);
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public ImmutableList<E> copy()
	{
		return new ArrayList<>((E[]) this.elements, this.size, true);
	}

	@NonNull
	@Override
	public MutableList<E> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public java.util.@NonNull List<E> toJava()
	{
		return Collections.unmodifiableList(super.toJava());
	}
}

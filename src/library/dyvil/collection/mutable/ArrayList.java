package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayList;
import dyvil.lang.LiteralConvertible;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
public class ArrayList<E> extends AbstractArrayList<E> implements MutableList<E>
{
	private static final long serialVersionUID = 5286872411535856904L;

	// Factory Methods

	@NonNull
	public static <E> ArrayList<E> apply()
	{
		return new ArrayList<>();
	}

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

	// Constructors

	public ArrayList()
	{
		super();
	}

	public ArrayList(int capacity)
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
	public MutableList<E> subList(int startIndex, int length)
	{
		List.rangeCheck(startIndex, this.size);
		List.rangeCheck(startIndex + length - 1, this.size);

		Object[] array = new Object[length];
		System.arraycopy(this.elements, startIndex, array, 0, length);
		return new ArrayList<>((E[]) array, length, true);
	}

	@NonNull
	@Override
	public MutableList<E> copy(int capacity)
	{
		Object[] newArray = new Object[Math.max(this.size, capacity)];
		System.arraycopy(this.elements, 0, newArray, 0, this.size);
		return new ArrayList<>((E[]) newArray, this.size, true);
	}

	@NonNull
	@Override
	public MutableList<E> reversed()
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
	public void addElement(E element)
	{
		this.addInternal(element);
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.elements[i] = null;
		}

		this.size = 0;
	}

	protected void resize(int newLength)
	{
		if (newLength < this.size)
		{
			for (int i = newLength; i < this.size; i++)
			{
				this.elements[i] = null;
			}
			this.size = newLength;
			return;
		}

		this.ensureCapacityInternal(newLength);
		this.size = newLength;
	}

	@Override
	public void ensureCapacity(int minSize)
	{
		this.ensureCapacityInternal(minSize);
	}

	@Override
	public void subscript_$eq(int index, E element)
	{
		List.rangeCheck(index, this.size);
		this.elements[index] = element;
	}

	@NonNull
	@Override
	public E set(int index, E element)
	{
		List.rangeCheck(index, this.size);
		final E oldValue = (E) this.elements[index];
		this.elements[index] = element;
		return oldValue;
	}

	@Nullable
	@Override
	public E setResizing(int index, E element)
	{
		if (index < 0)
		{
			List.rangeCheck(index, this.size);
			return null;
		}
		if (index >= this.size)
		{
			this.resize(index + 1);
		}

		E e = (E) this.elements[index];
		this.elements[index] = element;
		return e;
	}

	@Override
	public void insert(int index, E element)
	{
		if (index == this.size)
		{
			this.addElement(element);
			return;
		}
		List.rangeCheck(index, this.size);

		this.ensureCapacityInternal(this.size + 1);
		System.arraycopy(this.elements, index, this.elements, index + 1, this.size - index);
		this.elements[index] = element;
		this.size++;
	}

	@Override
	public boolean addAll(@NonNull Collection<? extends E> collection)
	{
		if (collection.isEmpty())
		{
			return false;
		}

		this.addAllInternal(collection);
		return true;
	}

	@Override
	public boolean remove(Object element)
	{
		boolean removed = false;
		for (int index = 0; index < this.size; index++)
		{
			if (Objects.equals(element, this.elements[index]))
			{
				int numMoved = --this.size - index;
				if (numMoved > 0)
				{
					System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
				}
				index--;
				removed = true;
				this.elements[this.size] = null;
			}
		}

		return removed;
	}

	@Override
	public void removeAt(int index)
	{
		List.rangeCheck(index, this.size);
		int numMoved = --this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
		}
		this.elements[this.size] = null;
	}

	@Override
	public boolean removeAll(@NonNull Collection<?> collection)
	{
		boolean removed = false;
		int index = 0;
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (!collection.contains(e))
			{
				array[index++] = e;
			}
			else
			{
				removed = true;
			}
		}
		this.elements = array;
		this.size = index;
		return removed;
	}

	@Override
	public boolean retainAll(@NonNull Collection<? extends E> collection)
	{
		boolean removed = false;
		int index = 0;
		Object[] array = new Object[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Object e = this.elements[i];
			if (collection.contains(e))
			{
				array[index++] = e;
			}
			else
			{
				removed = true;
			}
		}
		this.elements = array;
		this.size = index;
		return removed;
	}

	@Override
	public void filter(@NonNull Predicate<? super E> condition)
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
		this.elements = array;
		this.size = index;
	}

	@Override
	public void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.elements[i] = mapper.apply((E) this.elements[i]);
		}
	}

	@Override
	public void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		Object[] array = new Object[this.size << 2];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			for (E e : mapper.apply((E) this.elements[i]))
			{
				if (index >= array.length)
				{
					Object[] temp = new Object[index << 1];
					System.arraycopy(array, 0, temp, 0, index);
					array = temp;
				}
				array[index++] = e;
			}
		}

		this.elements = array;
		this.size = index;
	}

	@Override
	public void reverse()
	{
		for (int start = 0, end = this.size - 1; start <= end; start++, end--)
		{
			Object temp = this.elements[start];
			this.elements[start] = this.elements[end];
			this.elements[end] = temp;
		}
	}

	@Override
	public void sort()
	{
		Arrays.sort(this.elements, 0, this.size);
	}

	@Override
	public void sort(@NonNull Comparator<? super E> comparator)
	{
		Arrays.sort((E[]) this.elements, 0, this.size, comparator);
	}

	@Override
	public void distinguish()
	{
		this.size = Set.distinct(this.elements, this.size);
	}

	@Override
	public void distinguish(@NonNull Comparator<? super E> comparator)
	{
		this.size = Set.sortDistinct((E[]) this.elements, this.size, comparator);
	}

	@NonNull
	@Override
	public MutableList<E> copy()
	{
		return this.mutableCopy();
	}

	@NonNull
	@Override
	public ImmutableList<E> immutable()
	{
		return this.immutableCopy();
	}
}

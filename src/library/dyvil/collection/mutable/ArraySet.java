package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractArraySet;
import dyvil.lang.LiteralConvertible;

import java.util.Objects;
import java.util.function.Function;

@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
public class ArraySet<E> extends AbstractArraySet<E> implements MutableSet<E>
{
	private static final long serialVersionUID = -6676561653968567088L;

	// Factory Methods

	@NonNull
	public static <E> ArraySet<E> apply()
	{
		return new ArraySet<>();
	}

	@NonNull
	@SafeVarargs
	public static <E> ArraySet<E> apply(@NonNull E... elements)
	{
		return new ArraySet<>(elements, true);
	}

	@NonNull
	public static <E> ArraySet<E> from(E[] array)
	{
		return new ArraySet<>(array);
	}

	@NonNull
	public static <E> ArraySet<E> from(@NonNull Iterable<? extends E> iterable)
	{
		return new ArraySet<>(iterable);
	}

	@NonNull
	public static <E> ArraySet<E> from(@NonNull Collection<? extends E> collection)
	{
		return new ArraySet<>(collection);
	}

	@NonNull
	public static <E> ArraySet<E> from(@NonNull Set<? extends E> set)
	{
		return new ArraySet<>(set);
	}

	@NonNull
	public static <E> ArraySet<E> from(@NonNull AbstractArraySet<? extends E> arraySet)
	{
		return new ArraySet<>(arraySet);
	}

	// Constructors

	public ArraySet()
	{
		super();
	}

	public ArraySet(int capacity)
	{
		super(capacity);
	}

	public ArraySet(E[] elements)
	{
		super(elements);
	}

	public ArraySet(E @NonNull [] elements, int size)
	{
		super(elements, size);
	}

	public ArraySet(E @NonNull [] elements, boolean trusted)
	{
		super(elements, trusted);
	}

	public ArraySet(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}

	public ArraySet(@NonNull Iterable<? extends E> iterable)
	{
		super(iterable);
	}

	public ArraySet(@NonNull Collection<? extends E> collection)
	{
		super(collection);
	}

	public ArraySet(@NonNull Set<? extends E> set)
	{
		super(set);
	}

	public ArraySet(@NonNull AbstractArraySet<? extends E> arraySet)
	{
		super(arraySet);
	}

	// Implementation Methods

	@Override
	public void clear()
	{
		this.size = 0;
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] = null;
		}
	}

	@Override
	public boolean add(E element)
	{
		return this.addInternal(element);
	}

	@Override
	public boolean remove(Object element)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(this.elements[i], element))
			{
				this.removeAt(i);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void removeAt(int index)
	{
		int numMoved = --this.size - index;
		if (numMoved > 0)
		{
			System.arraycopy(this.elements, index + 1, this.elements, index, numMoved);
		}
		this.elements[this.size] = null;
	}

	@Override
	public void map(@NonNull Function<? super E, ? extends E> mapper)
	{
		this.mapImpl(mapper);
	}

	@Override
	public void flatMap(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends E>> mapper)
	{
		this.flatMapImpl(mapper);
	}

	@NonNull
	@Override
	public MutableSet<E> copy()
	{
		return this.mutableCopy();
	}

	@NonNull
	@Override
	public ImmutableSet<E> immutable()
	{
		return this.immutableCopy();
	}
}

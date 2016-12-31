package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.Set;
import dyvil.collection.impl.AbstractArraySet;
import dyvil.lang.LiteralConvertible;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@LiteralConvertible.FromArray
@Immutable
public class ArraySet<E> extends AbstractArraySet<E> implements ImmutableSet<E>
{
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private ArraySet<E> result;

		public Builder()
		{
			this.result = new ArraySet<>();
		}

		public Builder(int capacity)
		{
			this.result = new ArraySet<>(capacity);
		}

		@Override
		public void add(E element)
		{
			if (this.result == null)
			{
				throw new IllegalStateException("Already built");
			}

			this.result.addInternal(element);
		}

		@Override
		public ArraySet<E> build()
		{
			final ArraySet<E> result = this.result;
			this.result = null;
			return result;
		}
	}

	private static final long serialVersionUID = 5534347282324757054L;

	// Factory Methods

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
	public static <E> ArraySet<E> from(@NonNull AbstractArraySet<E> arraySet)
	{
		return new ArraySet<>(arraySet);
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

	protected ArraySet()
	{
		super();
	}

	protected ArraySet(int capacity)
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

	public ArraySet(@NonNull AbstractArraySet<E> arraySet)
	{
		super(arraySet);
	}

	// Implementation Methods

	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable Set");
	}

	@NonNull
	@Override
	public ImmutableSet<E> added(E element)
	{
		if (this.contains(element))
		{
			return this;
		}

		Object[] newArray = new Object[this.size + 1];
		System.arraycopy(this.elements, 0, newArray, 0, this.size);
		newArray[this.size] = element;
		return new ArraySet<>((E[]) newArray, this.size + 1, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> removed(Object element)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			final Object thisElement = this.elements[i];
			if (!Objects.equals(thisElement, element))
			{
				newArray[index++] = thisElement;
			}
		}
		return new ArraySet<>((E[]) newArray, index, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> difference(@NonNull Collection<?> collection)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = (E) this.elements[i];
			if (!collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet<>((E[]) newArray, index, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> intersection(@NonNull Collection<? extends E> collection)
	{
		Object[] newArray = new Object[Math.min(this.size, collection.size())];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			E element = (E) this.elements[i];
			if (collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet<>((E[]) newArray, index, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> union(@NonNull Collection<? extends E> collection)
	{
		int size = this.size;
		Object[] newArray = new Object[size + collection.size()];
		System.arraycopy(this.elements, 0, newArray, 0, this.size);
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newArray[size++] = element;
			}
		}
		return new ArraySet<>((E[]) newArray, size, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> symmetricDifference(@NonNull Collection<? extends E> collection)
	{
		Object[] newArray = new Object[this.size + collection.size()];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Object element = this.elements[i];
			if (!collection.contains(element))
			{
				newArray[index++] = element;
			}
		}
		for (E element : collection)
		{
			if (!this.contains(element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet<>((E[]) newArray, index, true);
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	public <R> ImmutableSet<R> mapped(@NonNull Function<? super E, ? extends R> mapper)
	{
		ArraySet<R> copy = (ArraySet<R>) this.copy();
		copy.mapImpl((Function) mapper);
		return copy;
	}

	@NonNull
	@Override
	@SuppressWarnings("unchecked")
	public <R> ImmutableSet<R> flatMapped(@NonNull Function<? super E, ? extends @NonNull Iterable<? extends R>> mapper)
	{
		ArraySet<R> copy = (ArraySet<R>) this.copy();
		copy.flatMapImpl((Function) mapper);
		return copy;
	}

	@NonNull
	@Override
	public ImmutableSet<E> filtered(@NonNull Predicate<? super E> condition)
	{
		Object[] newArray = new Object[this.size];
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Object element = this.elements[i];
			if (condition.test((E) element))
			{
				newArray[index++] = element;
			}
		}
		return new ArraySet<>((E[]) newArray, index, true);
	}

	@NonNull
	@Override
	public ImmutableSet<E> copy()
	{
		return this.immutableCopy();
	}

	@NonNull
	@Override
	public MutableSet<E> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public java.util.@NonNull Set<E> toJava()
	{
		return Collections.unmodifiableSet(super.toJava());
	}
}

package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.Collection;
import dyvil.collection.ImmutableSet;
import dyvil.collection.MutableSet;
import dyvil.collection.impl.AbstractArraySet;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@ArrayConvertible
@Immutable
public class ArraySet<E> extends AbstractArraySet<E> implements ImmutableSet<E>
{
	public static class Builder<E> implements ImmutableSet.Builder<E>
	{
		private Object[] elements;
		private int      size;

		public Builder()
		{
			this.elements = new Object[DEFAULT_CAPACITY];
		}

		public Builder(int capacity)
		{
			this.elements = new Object[capacity];
		}

		@Override
		public void add(E element)
		{
			if (this.size < 0)
			{
				throw new IllegalStateException("Already built");
			}

			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(this.elements[i], element))
				{
					this.elements[i] = element;
					return;
				}
			}

			int index = this.size++;
			if (index >= this.elements.length)
			{
				Object[] temp = new Object[(int) (this.size * 1.1F)];
				System.arraycopy(this.elements, 0, temp, 0, index);
				this.elements = temp;
			}
			this.elements[index] = element;
		}

		@Override
		public ArraySet<E> build()
		{
			if (this.size < 0)
			{
				return null;
			}

			ArraySet<E> set = new ArraySet<>((E[]) this.elements, this.size, true);
			this.size = -1;
			return set;
		}
	}

	private static final long serialVersionUID = 5534347282324757054L;

	@SafeVarargs
	public static <E> ArraySet<E> apply(E... elements)
	{
		return new ArraySet<>(elements, true);
	}

	public static <E> ArraySet<E> fromArray(E[] elements)
	{
		return new ArraySet<>(elements);
	}

	public static <E> Builder<E> builder()
	{
		return new Builder<>();
	}

	public static <E> Builder<E> builder(int capacity)
	{
		return new Builder<>(capacity);
	}

	@SafeVarargs
	public ArraySet(E... elements)
	{
		super(elements);
	}
	
	public ArraySet(E[] elements, int size)
	{
		super(elements, size);
	}
	
	public ArraySet(E[] elements, boolean trusted)
	{
		super(elements, elements.length, trusted);
	}
	
	public ArraySet(E[] elements, int size, boolean trusted)
	{
		super(elements, size, trusted);
	}
	
	public ArraySet(Collection<E> elements)
	{
		super(elements);
	}
	
	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("removeAt() on Immutable Set");
	}
	
	@Override
	public ImmutableSet<E> $plus(E element)
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
	
	@Override
	public ImmutableSet<E> $minus(Object element)
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
	
	@Override
	public ImmutableSet<? extends E> $minus$minus(Collection<?> collection)
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
	
	@Override
	public ImmutableSet<? extends E> $amp(Collection<? extends E> collection)
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
	
	@Override
	public ImmutableSet<? extends E> $bar(Collection<? extends E> collection)
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
	
	@Override
	public ImmutableSet<? extends E> $up(Collection<? extends E> collection)
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
	
	@Override
	@SuppressWarnings("unchecked")
	public <R> ImmutableSet<R> mapped(Function<? super E, ? extends R> mapper)
	{
		ArraySet<R> copy = (ArraySet<R>) this.copy();
		copy.mapImpl((Function) mapper);
		return copy;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> ImmutableSet<R> flatMapped(Function<? super E, ? extends Iterable<? extends R>> mapper)
	{
		ArraySet<R> copy = (ArraySet<R>) this.copy();
		copy.flatMapImpl((Function) mapper);
		return copy;
	}
	
	@Override
	public ImmutableSet<E> filtered(Predicate<? super E> condition)
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
	
	@Override
	public ImmutableSet<E> copy()
	{
		return this.immutableCopy();
	}
	
	@Override
	public MutableSet<E> mutable()
	{
		return this.mutableCopy();
	}
	
	@Override
	public java.util.Set<E> toJava()
	{
		return Collections.unmodifiableSet(super.toJava());
	}
}

package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.math.MathUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import static dyvil.collection.impl.AbstractHashMap.*;

public abstract class AbstractHashSet<E> implements Set<E>
{
	protected static final class HashElement<E>
	{
		public E              element;
		public int            hash;
		public HashElement<E> next;

		public HashElement(E element, int hash)
		{
			this.element = element;
			this.hash = hash;
		}

		public HashElement(E element, int hash, HashElement<E> next)
		{
			this.element = element;
			this.hash = hash;
			this.next = next;
		}
	}

	private static final long serialVersionUID = -2574454530914084132L;

	protected transient int              size;
	protected transient HashElement<E>[] elements;

	// Constructors

	public AbstractHashSet()
	{
		this.elements = (HashElement<E>[]) new HashElement[DEFAULT_CAPACITY];
	}

	public AbstractHashSet(int capacity)
	{
		if (capacity < 0)
		{
			throw new IllegalArgumentException("Invalid Capacity: " + capacity);
		}
		this.elements = (HashElement<E>[]) new HashElement[MathUtils.nextPowerOf2(AbstractHashMap.grow(capacity))];
	}

	public AbstractHashSet(E @NonNull [] elements)
	{
		this(elements.length);
		for (E element : elements)
		{
			this.addInternal(element);
		}
	}

	public AbstractHashSet(@NonNull Iterable<? extends E> iterable)
	{
		this();
		this.addAllInternal(iterable);
	}

	public AbstractHashSet(@NonNull SizedIterable<? extends E> iterable)
	{
		this(iterable.size());
		this.addAllInternal((Iterable<? extends E>) iterable);
	}

	public AbstractHashSet(@NonNull Set<? extends E> set)
	{
		this(set.size());
		this.loadDistinct(set);
	}

	public AbstractHashSet(@NonNull AbstractHashSet<? extends E> elements)
	{
		this(elements.size);
		this.size = elements.size;

		final HashElement<E>[] hashElements = this.elements;
		final int length = hashElements.length;

		for (HashElement hashElement : elements.elements)
		{
			for (; hashElement != null; hashElement = hashElement.next)
			{
				final int hash = hashElement.hash;
				final int index = index(hash, length);
				hashElements[index] = new HashElement<>((E) hashElement.element, hash, hashElements[index]);
			}
		}
	}

	// Implementation Methods

	protected void flatten()
	{
		this.ensureCapacityInternal(this.elements.length << 1);
	}

	public void ensureCapacity(int newCapacity)
	{
		if (newCapacity > this.elements.length)
		{
			this.ensureCapacity(MathUtils.nextPowerOf2(newCapacity));
		}
	}

	protected void ensureCapacityInternal(int newCapacity)
	{
		HashElement<E>[] oldMap = this.elements;
		int oldCapacity = oldMap.length;

		// overflow-conscious code
		if (newCapacity - MAX_ARRAY_SIZE > 0)
		{
			if (oldCapacity == MAX_ARRAY_SIZE)
			{
				// Keep running with MAX_ARRAY_SIZE buckets
				return;
			}
			newCapacity = MAX_ARRAY_SIZE;
		}

		@SuppressWarnings("unchecked") HashElement<E>[] newMap = this.elements = new HashElement[newCapacity];

		for (int i = oldCapacity; i-- > 0; )
		{
			for (HashElement<E> e = oldMap[i]; e != null; )
			{
				int index = index(e.hash, newCapacity);
				HashElement<E> next = e.next;

				e.next = newMap[index];
				newMap[index] = e;
				e = next;
			}
		}

		this.updateThreshold(newCapacity);
	}

	protected void updateThreshold(int newCapacity)
	{
	}

	protected boolean addInternal(E element)
	{
		int hash = hash(element);
		int i = index(hash, this.elements.length);
		for (HashElement<E> e = this.elements[i]; e != null; e = e.next)
		{
			Object key;
			if (e.hash == hash && ((key = e.element) == element || Objects.equals(element, key)))
			{
				return false;
			}
		}

		this.addElement(hash, element, i);
		return true;
	}

	protected abstract void addElement(int hash, E element, int index);

	protected void addAllInternal(@NonNull Iterable<? extends E> iterable)
	{
		for (E element : iterable)
		{
			this.addInternal(element);
		}
	}

	protected void addAllInternal(@NonNull SizedIterable<? extends E> iterable)
	{
		this.ensureCapacity(this.size + iterable.size());
		this.addAllInternal((Iterable<? extends E>) iterable);
	}

	private void loadDistinct(@NonNull Iterable<? extends E> iterable)
	{
		final HashElement<E>[] hashElements = this.elements;
		final int length = hashElements.length;
		int size = 0;

		// Assume unique elements
		for (E element : iterable)
		{
			size++;
			int hash = hash(element);
			int index = index(hash, length);
			hashElements[index] = new HashElement<>(element, hash, hashElements[index]);
		}
		this.size = size;
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			@Nullable HashElement<E> next; // next entry to return
			@Nullable HashElement<E> current; // current entry
			int index; // current slot

			{
				HashElement<E>[] t = AbstractHashSet.this.elements;
				this.current = this.next = null;
				this.index = 0;

				// advance to first entry
				if (t != null && AbstractHashSet.this.size > 0)
				{
					this.advance(t);
				}
			}

			@Override
			public final boolean hasNext()
			{
				return this.next != null;
			}

			@Override
			public final E next()
			{
				HashElement<E>[] t;
				HashElement<E> e = this.next;
				if (e == null)
				{
					throw new NoSuchElementException();
				}
				if ((this.next = (this.current = e).next) == null && (t = AbstractHashSet.this.elements) != null)
				{
					this.advance(t);
				}
				return e.element;
			}

			private void advance(HashElement<E> @NonNull [] t)
			{
				//noinspection StatementWithEmptyBody
				while (this.index < t.length && (this.next = t[this.index++]) == null);
			}

			@Override
			public final void remove()
			{
				HashElement<E> e = this.current;
				if (e == null)
				{
					throw new IllegalStateException();
				}

				AbstractHashSet.this.removeElement(e);
				this.current = null;
			}
		};
	}

	protected void removeElement(@NonNull HashElement<E> element)
	{
		this.size--;
		int index = index(element.hash, this.elements.length);
		HashElement<E> e = this.elements[index];
		if (e == element)
		{
			this.elements[index] = element.next;
		}
		else
		{
			HashElement<E> prev;
			do
			{
				prev = e;
				e = e.next;
			}
			while (e != element);

			prev.next = element.next;
		}
	}

	@Override
	public void forEach(@NonNull Consumer<? super E> action)
	{
		for (HashElement<E> hashElement : this.elements)
		{
			for (; hashElement != null; hashElement = hashElement.next)
			{
				action.accept(hashElement.element);
			}
		}
	}

	@Override
	public boolean contains(@Nullable Object element)
	{
		if (element == null)
		{
			for (HashElement<E> hashElement = this.elements[0]; hashElement != null; hashElement = hashElement.next)
			{
				if (hashElement.element == null)
				{
					return true;
				}
			}

			return false;
		}

		int hash = hash(element);
		int index = index(hash, this.elements.length);
		for (HashElement<E> hashElement = this.elements[index]; hashElement != null; hashElement = hashElement.next)
		{
			if (hashElement.hash == hash && element.equals(hashElement.element))
			{
				return true;
			}
		}
		return false;
	}

	@NonNull
	@Override
	public MutableSet<E> mutableCopy()
	{
		return new dyvil.collection.mutable.HashSet<>(this);
	}

	@NonNull
	@Override
	public <R> MutableSet<R> emptyCopy()
	{
		return new dyvil.collection.mutable.HashSet<>();
	}

	@NonNull
	@Override
	public <RE> MutableSet<RE> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.HashSet<>(capacity);
	}

	@NonNull
	@Override
	public ImmutableSet<E> immutableCopy()
	{
		return new dyvil.collection.immutable.HashSet<>(this);
	}

	@Override
	public <RE> ImmutableSet.Builder<RE> immutableBuilder()
	{
		return dyvil.collection.immutable.HashSet.builder();
	}

	@Override
	public <RE> ImmutableSet.Builder<RE> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.HashSet.builder(capacity);
	}

	@Override
	public java.util.Set<E> toJava()
	{
		java.util.HashSet<E> set = new java.util.HashSet<>(this.size);
		for (E element : this)
		{
			set.add(element);
		}
		return set;
	}

	@NonNull
	@Override
	public String toString()
	{
		return Collection.collectionToString(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		return Set.setEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return Set.setHashCode(this);
	}

	private void writeObject(java.io.@NonNull ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		int len = this.elements.length;

		out.writeInt(this.size);
		out.writeInt(len);

		for (int i = 0; i < len; i++)
		{
			for (HashElement<E> element = this.elements[i]; element != null; element = element.next)
			{
				out.writeObject(element.element);
				out.writeByte(i);
			}
		}
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.size = in.readInt();
		int len = in.readInt();

		this.elements = (HashElement<E>[]) new HashElement[len];
		for (int i = 0; i < len; i++)
		{
			this.addInternal((E) in.readObject());
		}
	}
}

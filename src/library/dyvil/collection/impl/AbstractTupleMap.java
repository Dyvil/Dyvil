package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.math.MathUtils;
import dyvil.tuple.Tuple;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractTupleMap<K, V> implements Map<K, V>
{
	private static final long serialVersionUID = 1636602530347500387L;

	protected static final int DEFAULT_CAPACITY = 16;

	protected transient int                                  size;
	protected transient Tuple.@NonNull Of2<K, V> @NonNull [] entries;

	public AbstractTupleMap()
	{
		this.entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[DEFAULT_CAPACITY];
	}

	public AbstractTupleMap(int capacity)
	{
		this.entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[MathUtils.powerOfTwo(capacity)];
	}

	public AbstractTupleMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		this(entries.length);
		this.size = entries.length;
		for (int i = 0; i < entries.length; i++)
		{
			this.entries[i] = (Tuple.Of2<K, V>) entries[i].toTuple();
		}
	}

	@SuppressWarnings("SuspiciousSystemArraycopy")
	public AbstractTupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries)
	{
		this.size = entries.length;
		this.entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];
		System.arraycopy(entries, 0, this.entries, 0, this.size);
	}

	@SuppressWarnings("SuspiciousSystemArraycopy")
	public AbstractTupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries, int size)
	{
		this.size = size;
		this.entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[size];
		System.arraycopy(entries, 0, this.entries, 0, size);
	}

	public AbstractTupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries,
		                       @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.size = entries.length;
		this.entries = (Tuple.Of2<K, V>[]) entries;
	}

	public AbstractTupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries, int size,
		                       @SuppressWarnings("UnusedParameters") boolean trusted)
	{
		this.size = size;
		this.entries = (Tuple.Of2<K, V>[]) entries;
	}

	public AbstractTupleMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		this();
		this.loadEntries(iterable);
	}

	public AbstractTupleMap(@NonNull SizedIterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		this(iterable.size());
		this.loadEntries(iterable);
	}

	public AbstractTupleMap(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
	{
		this(set.size());
		this.loadDistinctEntries(set);
	}

	public AbstractTupleMap(@NonNull Map<? extends K, ? extends V> map)
	{
		this(map.size());
		this.loadDistinctEntries(map);
	}

	public AbstractTupleMap(@NonNull AbstractTupleMap<? extends K, ? extends V> tupleMap)
	{
		this.size = tupleMap.size;
		this.entries = (Tuple.Of2<K, V>[]) tupleMap.entries.clone();
	}

	private void loadEntries(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		for (Entry<? extends K, ? extends V> entry : iterable)
		{
			this.putInternal((Tuple.Of2<K, V>) entry.toTuple());
		}
	}

	private void loadDistinctEntries(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		int index = 0;
		for (Entry<? extends K, ? extends V> entry : iterable)
		{
			this.entries[index++] = (Tuple.Of2<K, V>) entry.toTuple();
		}
		this.size = index;
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}

	@NonNull
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new Iterator<Entry<K, V>>()
		{
			private int index;

			@Override
			public boolean hasNext()
			{
				return this.index < AbstractTupleMap.this.size;
			}

			@Override
			public Entry<K, V> next()
			{
				return AbstractTupleMap.this.entries[this.index++];
			}

			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractTupleMap.this.removeAt(--this.index);
			}
		};
	}

	@NonNull
	@Override
	public Iterator<K> keyIterator()
	{
		return new Iterator<K>()
		{
			private int index;

			@Override
			public boolean hasNext()
			{
				return this.index < AbstractTupleMap.this.size;
			}

			@NonNull
			@Override
			public K next()
			{
				return AbstractTupleMap.this.entries[this.index++]._1;
			}

			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractTupleMap.this.removeAt(--this.index);
			}
		};
	}

	@NonNull
	@Override
	public Iterator<V> valueIterator()
	{
		return new Iterator<V>()
		{
			private int index;

			@Override
			public boolean hasNext()
			{
				return this.index < AbstractTupleMap.this.size;
			}

			@NonNull
			@Override
			public V next()
			{
				return AbstractTupleMap.this.entries[this.index++]._2;
			}

			@Override
			public void remove()
			{
				if (this.index <= 0)
				{
					throw new IllegalStateException();
				}
				AbstractTupleMap.this.removeAt(--this.index);
			}
		};
	}

	protected abstract void removeAt(int index);

	protected final V putInternal(Tuple.@NonNull Of2<K, V> tuple)
	{
		K key = tuple._1;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				V oldValue = entry._2;
				this.entries[i] = tuple;
				return oldValue;
			}
		}

		this.putNew(tuple);
		return null;
	}

	protected final void putNew(Tuple.Of2<K, V> tuple)
	{
		int index = this.size++;
		if (index >= this.entries.length)
		{
			int newCapacity = (int) (this.size * 1.1F);
			final Tuple.Of2<K, V>[] newEntries = (Tuple.Of2<K, V>[]) new Tuple.Of2[newCapacity];
			System.arraycopy(this.entries, 0, newEntries, 0, index);
			this.entries = newEntries;
		}
		this.entries[index] = tuple;
	}

	@Override
	public void forEach(@NonNull Consumer<? super Entry<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]);
		}
	}

	@Override
	public void forEach(@NonNull BiConsumer<? super K, ? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			action.accept(entry._1, entry._2);
		}
	}

	@Override
	public void forEachKey(@NonNull Consumer<? super K> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]._1);
		}
	}

	@Override
	public void forEachValue(@NonNull Consumer<? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]._2);
		}
	}

	@Override
	public boolean containsKey(@Nullable Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(@Nullable Object key, @Nullable Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				if (value == entry._2 || value != null && value.equals(entry._2))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsValue(@Nullable Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (value == entry._2 || value != null && value.equals(entry._2))
			{
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public V get(Object key)
	{
		final int index = this.getIndex(key);
		return index < 0 ? null : this.entries[index]._2;
	}

	public int getIndex(@Nullable Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return i;
			}
		}
		return -1;
	}

	@NonNull
	@Override
	public Option<V> getOption(@Nullable Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return new Some<>(entry._2);
			}
		}
		return (Option<V>) None.instance;
	}

	@Override
	public Entry<K, V> @NonNull [] toArray()
	{
		final Tuple.Of2<K, V>[] array = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];
		System.arraycopy(this.entries, 0, array, 0, this.size);
		return array;
	}

	@Override
	public void toArray(int index, @NonNull Entry<K, V> @NonNull [] store)
	{
		System.arraycopy(this.entries, 0, store, index, this.size);
	}

	@Override
	public void toKeyArray(int index, Object @NonNull [] store)
	{
		for (int i = 0; i < this.size; i++)
		{
			store[index++] = this.entries[i]._1;
		}
	}

	@Override
	public void toValueArray(int index, Object @NonNull [] store)
	{
		for (int i = 0; i < this.size; i++)
		{
			store[index++] = this.entries[i]._2;
		}
	}

	@NonNull
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new dyvil.collection.mutable.TupleMap<>();
	}

	@NonNull
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.TupleMap<>(capacity);
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutableCopy()
	{
		return new dyvil.collection.mutable.TupleMap<>(this);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> immutableCopy()
	{
		return new dyvil.collection.immutable.TupleMap<>(this);
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder()
	{
		return dyvil.collection.immutable.TupleMap.builder();
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.TupleMap.builder(capacity);
	}

	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.LinkedHashMap<K, V> map = new java.util.LinkedHashMap<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			map.put(entry._1, entry._2);
		}
		return map;
	}

	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return Map.EMPTY_STRING;
		}

		final StringBuilder builder = new StringBuilder(Map.START_STRING);
		Tuple.Of2<K, V> entry = this.entries[0];

		builder.append(entry._1).append(Map.KEY_VALUE_SEPARATOR_STRING).append(entry._2);
		for (int i = 1; i < this.size; i++)
		{
			entry = this.entries[i];
			builder.append(Map.ENTRY_SEPARATOR_STRING).append(entry._1).append(Map.KEY_VALUE_SEPARATOR_STRING)
			       .append(entry._2);
		}
		return builder.append(Map.END_STRING).toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		return Map.mapEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return Map.mapHashCode(this);
	}

	private void writeObject(java.io.@NonNull ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		out.writeInt(this.size);
		for (int i = 0; i < this.size; i++)
		{
			out.writeObject(this.entries[i]);
		}
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.size = in.readInt();
		this.entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];
		for (int i = 0; i < this.size; i++)
		{
			this.entries[i] = (Tuple.Of2<K, V>) in.readObject();
		}
	}
}

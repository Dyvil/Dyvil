package dyvil.collection.impl;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.reflect.EnumReflection;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;
import dyvilx.lang.model.type.Type;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class AbstractEnumMap<K extends Enum<K>, V> implements Map<K, V>
{
	protected class EnumEntry implements Entry<K, V>
	{
		private static final long serialVersionUID = 4125489955668261409L;

		int index;

		EnumEntry(int index)
		{
			this.index = index;
		}

		@Override
		public K getKey()
		{
			return AbstractEnumMap.this.keys[this.index];
		}

		@NonNull
		@Override
		public V getValue()
		{
			return (V) AbstractEnumMap.this.values[this.index];
		}

		@NonNull
		@Override
		public String toString()
		{
			return AbstractEnumMap.this.keys[this.index] + " -> " + AbstractEnumMap.this.values[this.index];
		}

		@Override
		public boolean equals(Object obj)
		{
			return Entry.entryEquals(this, obj);
		}

		@Override
		public int hashCode()
		{
			return Entry.entryHashCode(this);
		}
	}

	protected abstract class EnumIterator<E> implements Iterator<E>
	{
		private int     index;
		private boolean indexValid;

		@Override
		public boolean hasNext()
		{
			Object[] tab = AbstractEnumMap.this.values;
			for (int i = this.index; i < tab.length; i++)
			{
				Object key = tab[i];
				if (key != null)
				{
					this.index = i;
					return this.indexValid = true;
				}
			}
			this.index = tab.length;
			return false;
		}

		protected int nextIndex()
		{
			if (!this.indexValid && !this.hasNext())
			{
				throw new NoSuchElementException();
			}

			this.indexValid = false;
			return this.index++;
		}

		@Override
		public void remove()
		{
			if (this.index == 0)
			{
				throw new IllegalStateException();
			}

			AbstractEnumMap.this.removeAt(--this.index);
		}
	}

	private static final long serialVersionUID = 7946242151088885999L;

	protected transient Class<K> type;
	protected transient K[]      keys;
	protected transient Object[] values;
	protected transient int      size;

	// Constructors

	protected AbstractEnumMap(Class<K> type, K[] keys, V[] values, int size)
	{
		this.type = type;
		this.keys = keys;
		this.values = values;
		this.size = size;
	}

	public AbstractEnumMap(@NonNull Type<K> type)
	{
		this(type.erasure());
	}

	public AbstractEnumMap(Class<K> type)
	{
		this.keys = EnumReflection.getEnumConstants(type);
		this.values = new Object[this.keys.length];
		this.type = type;
	}

	public AbstractEnumMap(Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		this(getKeyType(entries));

		for (Entry<? extends K, ? extends V> entry : entries)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}

	public AbstractEnumMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> map)
	{
		this(getKeyType(map));

		this.putAllInternal(map);
	}

	@SuppressWarnings("unchecked")
	public AbstractEnumMap(@NonNull AbstractEnumMap<? extends K, ? extends V> map)
	{
		this.keys = map.keys;
		this.type = (Class<K>) map.type;
		this.values = map.values.clone();
		this.size = map.size;
	}

	// Implementation Methods

	protected static <K extends Enum<K>> Class<K> getKeyType(@Nullable K key)
	{
		if (key != null)
		{
			return key.getDeclaringClass();
		}
		throw new IllegalArgumentException("Invalid Enum Map - Could not get Enum type");
	}

	protected static <K extends Enum<K>> Class<K> getKeyType(Entry<? extends K, ?> @NonNull [] array)
	{
		for (Entry<? extends K, ?> entry : array)
		{
			final K key = entry.getKey();
			if (key != null)
			{
				return key.getDeclaringClass();
			}
		}

		throw new IllegalArgumentException("Invalid Enum Map - Could not get Enum type");
	}

	protected static <K extends Enum<K>> Class<K> getKeyType(@NonNull Iterable<? extends Entry<? extends K, ?>> iterable)
	{
		for (Entry<? extends K, ?> entry : iterable)
		{
			final K key = entry.getKey();
			if (key != null)
			{
				return key.getDeclaringClass();
			}
		}
		throw new IllegalArgumentException("Invalid Enum Map - Could not get Enum type");
	}

	protected void putInternal(@NonNull K key, V value)
	{
		int index = key.ordinal();

		if (this.values[index] == null)
		{
			this.size++;
		}
		this.values[index] = value;
	}

	private void putAllInternal(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> map)
	{
		for (Entry<? extends K, ? extends V> entry : map)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}

	protected static boolean checkType(Class<?> type, @Nullable Object key)
	{
		return key != null && key.getClass() == type;
	}

	protected static int index(@NonNull Object key)
	{
		return ((Enum) key).ordinal();
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@NonNull
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new EnumIterator<Entry<K, V>>()
		{
			@NonNull
			@Override
			public Entry<K, V> next()
			{
				return new EnumEntry(this.nextIndex());
			}

			@NonNull
			@Override
			public String toString()
			{
				return "EntryIterator(" + AbstractEnumMap.this + ")";
			}
		};
	}

	@NonNull
	@Override
	public Iterator<K> keyIterator()
	{
		return new EnumIterator<K>()
		{
			@Override
			public K next()
			{
				return AbstractEnumMap.this.keys[this.nextIndex()];
			}

			@NonNull
			@Override
			public String toString()
			{
				return "KeyIterator(" + AbstractEnumMap.this + ")";
			}
		};
	}

	@NonNull
	@Override
	public Iterator<V> valueIterator()
	{
		return new EnumIterator<V>()
		{
			@NonNull
			@Override
			public V next()
			{
				return (V) AbstractEnumMap.this.values[this.nextIndex()];
			}

			@NonNull
			@Override
			public String toString()
			{
				return "ValueIterator(" + AbstractEnumMap.this + ")";
			}
		};
	}

	protected abstract void removeAt(int index);

	@Override
	public boolean containsKey(@NonNull Object key)
	{
		return checkType(this.type, key) && this.values[index(key)] != null;
	}

	@Override
	public boolean contains(@NonNull Object key, Object value)
	{
		if (!checkType(this.type, key))
		{
			return false;
		}

		Object v = this.values[((Enum) key).ordinal()];
		return Objects.equals(v, value);
	}

	@Override
	public boolean containsValue(Object value)
	{
		for (Object thisValue : this.values)
		{
			if (Objects.equals(thisValue, value))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public V get(@NonNull Object key)
	{
		if (!checkType(this.type, key))
		{
			return null;
		}

		return (V) this.values[index(key)];
	}

	@Nullable
	@Override
	public Entry<K, V> getEntry(@NonNull Object key)
	{
		if (!checkType(this.type, key))
		{
			return null;
		}

		final int index = index(key);
		if (this.values[index] == null)
		{
			return null;
		}
		return new Entry<K, V>()
		{
			@NonNull
			@Override
			public K getKey()
			{
				return (K) key;
			}

			@NonNull
			@Override
			public V getValue()
			{
				return (V) AbstractEnumMap.this.values[index];
			}
		};
	}

	@NonNull
	@Override
	public Option<V> getOption(@NonNull Object key)
	{
		if (!checkType(this.type, key))
		{
			return (Option<V>) None.instance;
		}

		return new Some<>((V) this.values[index(key)]);
	}

	@NonNull
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return MutableMap.apply();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return MutableMap.withCapacity(capacity);
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutableCopy()
	{
		return new dyvil.collection.mutable.EnumMap<>(this);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> immutableCopy()
	{
		return new dyvil.collection.immutable.EnumMap<>(this);
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder()
	{
		return ImmutableMap.builder();
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return ImmutableMap.builder();
	}

	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.EnumMap<K, V> map = new java.util.EnumMap<>(this.type);
		for (int i = 0; i < this.keys.length; i++)
		{
			V value = (V) this.values[i];
			if (value != null)
			{
				map.put(this.keys[i], value);
			}
		}
		return map;
	}

	@Override
	public String toString()
	{
		return Map.mapToString(this);
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

		out.writeObject(this.type);
		out.writeInt(this.size);

		int entriesToBeWritten = this.size;
		for (int i = 0; entriesToBeWritten > 0; i++)
		{
			if (this.values[i] != null)
			{
				out.writeObject(this.keys[i]);
				out.writeObject(this.values[i]);
				entriesToBeWritten--;
			}
		}
	}

	private void readObject(java.io.@NonNull ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.type = (Class<K>) in.readObject();
		this.keys = EnumReflection.getEnumConstants(this.type);
		this.values = new Object[this.keys.length];

		int size = in.readInt();

		for (int i = 0; i < size; i++)
		{
			K key = (K) in.readObject();
			V value = (V) in.readObject();
			this.putInternal(key, value);
		}
	}
}

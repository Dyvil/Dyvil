package dyvil.collection.impl;

import dyvil.collection.*;
import dyvil.math.MathUtils;
import dyvil.util.ImmutableException;
import dyvil.util.None;
import dyvil.util.Option;
import dyvil.util.Some;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractIdentityHashMap<K, V> implements Map<K, V>
{
	protected final class TableEntry implements Entry<K, V>
	{
		private static final long serialVersionUID = 6124362820238071432L;

		protected int index;

		public TableEntry(int index)
		{
			this.index = index;
		}

		@Override
		public K getKey()
		{
			return (K) unmaskNull(AbstractIdentityHashMap.this.table[this.index]);
		}

		@Override
		public V getValue()
		{
			return (V) AbstractIdentityHashMap.this.table[this.index + 1];
		}

		@Override
		public String toString()
		{
			return unmaskNull(AbstractIdentityHashMap.this.table[this.index]) + " -> "
				       + AbstractIdentityHashMap.this.table[this.index + 1];
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

	protected abstract class TableIterator<E> implements Iterator<E>
	{
		protected int index             =
			AbstractIdentityHashMap.this.size != 0 ? 0 : AbstractIdentityHashMap.this.table.length;
		protected int lastReturnedIndex = -1;
		protected boolean indexValid;
		protected Object[] traversalTable = AbstractIdentityHashMap.this.table;

		@Override
		public boolean hasNext()
		{
			Object[] tab = this.traversalTable;
			for (int i = this.index; i < tab.length; i += 2)
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
			this.lastReturnedIndex = this.index;
			this.index += 2;
			return this.lastReturnedIndex;
		}

		@Override
		public void remove()
		{
			if (this.lastReturnedIndex == -1)
			{
				throw new IllegalStateException();
			}
			if (AbstractIdentityHashMap.this.isImmutable())
			{
				throw new ImmutableException("Iterator.remove() on Immutable Map");
			}

			int deletedSlot = this.lastReturnedIndex;
			this.lastReturnedIndex = -1;
			// back up index to revisit new contents after deletion
			this.index = deletedSlot;
			this.indexValid = false;

			Object[] tab = this.traversalTable;
			int len = tab.length;

			int d = deletedSlot;
			Object key = tab[d];
			tab[d] = null; // vacate the slot
			tab[d + 1] = null;

			if (tab != AbstractIdentityHashMap.this.table)
			{
				AbstractIdentityHashMap.this.removeKey(key);
				return;
			}

			AbstractIdentityHashMap.this.size--;

			Object item;
			for (int i = nextKeyIndex(d, len); (item = tab[i]) != null; i = nextKeyIndex(i, len))
			{
				int r = index(item, len);
				// See closeDeletion for explanation of this conditional
				if (i < r && (r <= d || d <= i) || r <= d && d <= i)
				{
					if (i < deletedSlot && d >= deletedSlot
						    && this.traversalTable == AbstractIdentityHashMap.this.table)
					{
						int remaining = len - deletedSlot;
						Object[] newTable = new Object[remaining];
						System.arraycopy(tab, deletedSlot, newTable, 0, remaining);
						this.traversalTable = newTable;
						this.index = 0;
					}

					tab[d] = item;
					tab[d + 1] = tab[i + 1];
					tab[i] = null;
					tab[i + 1] = null;
					d = i;
				}
			}
		}
	}

	private static final long serialVersionUID = -2493470311862510577L;

	protected static final int   DEFAULT_CAPACITY    = 12;
	protected static final float DEFAULT_LOAD_FACTOR = 2F / 3F;

	protected static final Object NULL = new Object();

	protected transient Object[] table;
	protected transient int      size;

	// Constructors

	public AbstractIdentityHashMap()
	{
		this.table = new Object[DEFAULT_CAPACITY];
	}

	public AbstractIdentityHashMap(int capacity)
	{
		this.table = new Object[MathUtils.powerOfTwo(AbstractHashMap.grow(capacity) << 1)];
	}

	public AbstractIdentityHashMap(Entry<? extends K, ? extends V>[] entries)
	{
		this(entries.length);
		for (Entry<? extends K, ? extends V> entry : entries)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}

	public AbstractIdentityHashMap(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		this();
		this.putAllInternal(iterable);
	}

	public AbstractIdentityHashMap(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		this();
		// Call the putAllInternal(Iterable) method to avoid redundant ensureCapacity call
		this.putAllInternal((Iterable<? extends Entry<? extends K, ? extends V>>) iterable);
	}

	public AbstractIdentityHashMap(Set<? extends Entry<? extends K, ? extends V>> set)
	{
		this(set.size());
		this.loadDistinctEntries(set);
	}

	public AbstractIdentityHashMap(Map<? extends K, ? extends V> map)
	{
		this(map.size());
		this.loadDistinctEntries(map);
	}

	public AbstractIdentityHashMap(AbstractIdentityHashMap<? extends K, ? extends V> identityHashMap)
	{
		this.size = identityHashMap.size;
		this.table = identityHashMap.table.clone();
	}

	// Implementation Methods

	public static Object maskNull(Object o)
	{
		return o == null ? NULL : o;
	}

	public static Object unmaskNull(Object o)
	{
		return o == NULL ? null : o;
	}

	public static int index(Object x, int length)
	{
		int h = System.identityHashCode(x);
		h = (h << 1) - (h << 8); // Multiply by -127
		return h & length - 1;
	}

	public static int nextKeyIndex(int i, int len)
	{
		return i + 2 < len ? i + 2 : 0;
	}

	protected void flatten()
	{
		this.ensureCapacityInternal(this.table.length << 1);
	}

	public void ensureCapacity(int newCapacity)
	{
		if (newCapacity > this.table.length >> 1)
		{
			this.ensureCapacityInternal(MathUtils.powerOfTwo(newCapacity) << 1);
		}
	}

	protected void ensureCapacityInternal(int newCapacity)
	{
		Object[] oldTable = this.table;
		int oldLength = oldTable.length;
		if (newCapacity - AbstractHashMap.MAX_ARRAY_SIZE > 0)
		{
			if (oldLength == AbstractHashMap.MAX_ARRAY_SIZE)
			{
				return;
			}
			newCapacity = AbstractHashMap.MAX_ARRAY_SIZE;
		}

		Object[] newTable = new Object[newCapacity];

		for (int j = 0; j < oldLength; j += 2)
		{
			Object key = oldTable[j];
			if (key != null)
			{
				Object value = oldTable[j + 1];
				oldTable[j] = null;
				oldTable[j + 1] = null;
				int i = index(key, newCapacity);
				while (newTable[i] != null)
				{
					i = nextKeyIndex(i, newCapacity);
				}
				newTable[i] = key;
				newTable[i + 1] = value;
			}
		}
		this.table = newTable;

		this.updateThreshold(newCapacity >> 1);
	}

	protected void updateThreshold(int newCapacity)
	{
	}

	protected V putInternal(K key, V value)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);

		Object item;
		while ((item = tab[i]) != null)
		{
			if (item == k)
			{
				V oldValue = (V) tab[i + 1];
				tab[i + 1] = value;
				return oldValue;
			}
			i = nextKeyIndex(i, len);
		}

		this.addEntry(i, k, value);
		return null;
	}

	protected void addEntry(int index, Object key, V value)
	{
		this.table[index] = key;
		this.table[index + 1] = value;

		if (++this.size >= (this.table.length >> 1) * DEFAULT_LOAD_FACTOR)
		{
			this.flatten();
		}
	}

	protected void putAllInternal(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		for (Entry<? extends K, ? extends V> entry : iterable)
		{
			this.putInternal(entry.getKey(), entry.getValue());
		}
	}

	protected void putAllInternal(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		this.ensureCapacity(this.size + iterable.size());
		this.putAllInternal((Iterable<? extends Entry<? extends K, ? extends V>>) iterable);
	}

	private void loadDistinctEntries(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		int index = 0;
		int size = 0;
		for (Entry<? extends K, ? extends V> entry : iterable)
		{
			this.table[index++] = entry.getKey();
			this.table[index++] = entry.getValue();
			size++;
		}
		this.size = size;
	}

	@Override
	public int size()
	{
		return this.size;
	}

	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new TableIterator<Entry<K, V>>()
		{
			@Override
			public Entry<K, V> next()
			{
				return new TableEntry(this.nextIndex());
			}

			@Override
			public String toString()
			{
				return "EntryIterator(" + AbstractIdentityHashMap.this + ")";
			}
		};
	}

	@Override
	public Iterator<K> keyIterator()
	{
		return new TableIterator<K>()
		{
			@Override
			public K next()
			{
				return (K) AbstractIdentityHashMap.this.table[this.nextIndex()];
			}

			@Override
			public String toString()
			{
				return "KeyIterator(" + AbstractIdentityHashMap.this + ")";
			}
		};
	}

	@Override
	public Iterator<V> valueIterator()
	{
		return new TableIterator<V>()
		{
			@Override
			public V next()
			{
				return (V) AbstractIdentityHashMap.this.table[this.nextIndex() + 1];
			}

			@Override
			public String toString()
			{
				return "ValueIterator(" + AbstractIdentityHashMap.this + ")";
			}
		};
	}

	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				action.accept((K) unmaskNull(key), (V) tab[i + 1]);
			}
		}
	}

	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				action.accept(new TableEntry(i));
			}
		}
	}

	@Override
	public void forEachKey(Consumer<? super K> action)
	{
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				action.accept((K) unmaskNull(key));
			}
		}
	}

	@Override
	public void forEachValue(Consumer<? super V> action)
	{
		Object[] tab = this.table;
		for (int i = 1; i < tab.length; i += 2)
		{
			if (tab[i - 1] != null)
			{
				action.accept((V) tab[i]);
			}
		}
	}

	@Override
	public boolean containsKey(Object key)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				return true;
			}
			if (item == null)
			{
				return false;
			}
			i = nextKeyIndex(i, len);
		}
	}

	@Override
	public boolean contains(Object key, Object value)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				return tab[i + 1] == value;
			}
			if (item == null)
			{
				return false;
			}
			i = nextKeyIndex(i, len);
		}
	}

	@Override
	public boolean containsValue(Object value)
	{
		Object[] tab = this.table;
		for (int i = 1; i < tab.length; i += 2)
		{
			if (tab[i] == value && tab[i - 1] != null)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public V get(Object key)
	{
		Object k = maskNull(key);
		Object[] tab = this.table;
		int len = tab.length;
		int i = index(k, len);
		while (true)
		{
			Object item = tab[i];
			if (item == k)
			{
				return (V) tab[i + 1];
			}
			if (item == null)
			{
				return null;
			}
			i = nextKeyIndex(i, len);
		}
	}

	@Override
	public Option<V> getOption(Object key)
	{
		final Object maskedKey = maskNull(key);
		final Object[] table = this.table;
		final int len = table.length;

		int i = index(maskedKey, len);
		while (true)
		{
			Object item = table[i];
			if (item == maskedKey)
			{
				return new Some<>((V) table[i + 1]);
			}
			if (item == null)
			{
				return (Option<V>) None.instance;
			}
			i = nextKeyIndex(i, len);
		}
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new dyvil.collection.mutable.IdentityHashMap<>();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return new dyvil.collection.mutable.IdentityHashMap<>(capacity);
	}

	@Override
	public MutableMap<K, V> mutableCopy()
	{
		return new dyvil.collection.mutable.IdentityHashMap<>(this);
	}

	@Override
	public ImmutableMap<K, V> immutableCopy()
	{
		return new dyvil.collection.immutable.IdentityHashMap<>(this);
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder()
	{
		return dyvil.collection.immutable.IdentityHashMap.builder();
	}

	@Override
	public <RK, RV> ImmutableMap.Builder<RK, RV> immutableBuilder(int capacity)
	{
		return dyvil.collection.immutable.IdentityHashMap.builder(capacity);
	}

	@Override
	public java.util.Map<K, V> toJava()
	{
		java.util.IdentityHashMap<K, V> map = new java.util.IdentityHashMap<>(this.size);
		Object[] tab = this.table;
		for (int i = 0; i < tab.length; i += 2)
		{
			Object key = tab[i];
			if (key != null)
			{
				map.put((K) unmaskNull(key), (V) tab[i + 1]);
			}
		}
		return map;
	}

	@Override
	public String toString()
	{
		if (this.size == 0)
		{
			return Map.EMPTY_STRING;
		}

		final StringBuilder builder = new StringBuilder(Map.START_STRING);
		final Object[] table = this.table;

		for (int i = 0; i < table.length; i += 2)
		{
			final Object key = table[i];
			if (key != null)
			{
				builder.append(unmaskNull(key)).append(Map.KEY_VALUE_SEPARATOR_STRING).append(table[i + 1])
				       .append(Map.ENTRY_SEPARATOR_STRING);
			}
		}

		final int len = builder.length();
		return builder.replace(len - Map.ENTRY_SEPARATOR_STRING.length(), len, Map.END_STRING).toString();
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

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		out.defaultWriteObject();

		int len = this.table.length;

		out.writeInt(this.size);
		out.writeInt(len);

		// Write (size) key-value pairs, sequentially
		for (int i = 0; i < len; i += 2)
		{
			// Avoid the NULL object
			Object key = this.table[i];
			if (key != null)
			{
				out.writeObject(unmaskNull(key));
				out.writeObject(this.table[i + 1]);
			}
		}
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();

		this.size = in.readInt();
		this.table = new Object[in.readInt()];

		// Read (size) key-value pairs and put them in this map
		for (int i = 0; i < this.size; i += 2)
		{
			this.putInternal((K) in.readObject(), (V) in.readObject());
		}
	}
}

package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractHashMap;
import dyvil.lang.LiteralConvertible;
import dyvil.ref.ObjectRef;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromColonOperator(methodName = "singleton")
@LiteralConvertible.FromArray
public class HashMap<K, V> extends AbstractHashMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = -5390749229591621243L;

	private           float loadFactor;
	private transient int   threshold;

	// Factory Methods

	@NonNull
	public static <K, V> HashMap<K, V> singleton(K key, V value)
	{
		final HashMap<K, V> result = new HashMap<>();
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	public static <K, V> HashMap<K, V> apply()
	{
		return new HashMap<>();
	}

	@NonNull
	@SafeVarargs
	public static <K, V> HashMap<K, V> apply(@NonNull Entry<K, V>... entries)
	{
		return new HashMap<>(entries);
	}

	@NonNull
	public static <K, V> HashMap<K, V> from(Entry<? extends K, ? extends V> @NonNull [] array)
	{
		return new HashMap<>(array);
	}

	@NonNull
	public static <K, V> HashMap<K, V> from(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new HashMap<>(iterable);
	}

	@NonNull
	public static <K, V> HashMap<K, V> from(@NonNull SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new HashMap<>(iterable);
	}

	@NonNull
	public static <K, V> HashMap<K, V> from(@NonNull Set<? extends Entry<? extends K, ? extends V>> set)
	{
		return new HashMap<>(set);
	}

	@NonNull
	public static <K, V> HashMap<K, V> from(@NonNull Map<? extends K, ? extends V> map)
	{
		return new HashMap<>(map);
	}

	@NonNull
	public static <K, V> HashMap<K, V> from(@NonNull AbstractHashMap<? extends K, ? extends V> hashMap)
	{
		return new HashMap<>(hashMap);
	}

	// Constructors

	public HashMap()
	{
		this(DEFAULT_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public HashMap(int capacity)
	{
		this(capacity, DEFAULT_LOAD_FACTOR);
	}

	public HashMap(float loadFactor)
	{
		this(DEFAULT_CAPACITY, loadFactor);
	}

	public HashMap(int capacity, float loadFactor)
	{
		super(capacity);
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
		{
			throw new IllegalArgumentException("Invalid Load Factor: " + loadFactor);
		}

		this.loadFactor = loadFactor;
		this.threshold = (int) Math.min(capacity * loadFactor, MAX_ARRAY_SIZE + 1);
	}

	public HashMap(Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
		this.defaultLoadFactor();
	}

	public HashMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
		this.defaultLoadFactor();
	}

	public HashMap(@NonNull SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
		this.defaultLoadFactor();
	}

	public HashMap(@NonNull Set<? extends Entry<? extends K, ? extends V>> set)
	{
		super(set);
		this.defaultLoadFactor();
	}

	public HashMap(@NonNull Map<? extends K, ? extends V> map)
	{
		super(map);
		this.defaultLoadFactor();
	}

	public HashMap(@NonNull AbstractHashMap<? extends K, ? extends V> map)
	{
		super(map);
		this.defaultLoadFactor();
	}

	// Implementation Methods

	private void defaultLoadFactor()
	{
		this.loadFactor = DEFAULT_LOAD_FACTOR;
		this.threshold = (int) (this.entries.length * DEFAULT_LOAD_FACTOR);
	}

	@Override
	protected void updateThreshold(int newCapacity)
	{
		this.threshold = (int) Math.min(newCapacity * this.loadFactor, MAX_ARRAY_SIZE + 1);
	}

	@Override
	public void clear()
	{
		this.size = 0;
		int length = this.entries.length;
		for (int i = 0; i < length; i++)
		{
			this.entries[i] = null;
		}
	}

	@Override
	public void subscript_$eq(K key, V value)
	{
		this.putInternal(key, value);
	}

	@NonNull
	@Override
	public ObjectRef<V> subscript_$amp(K key)
	{
		return this.getEntryInternal(key);
	}

	@Nullable
	@Override
	public V put(@Nullable K key, V value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				V oldValue = e.value;
				e.value = value;
				return oldValue;
			}
		}

		this.addEntry(hash, key, value, i);
		return null;
	}

	@Override
	protected void addEntry(int hash, K key, V value, int index)
	{
		HashEntry<K, V>[] tab = this.entries;
		if (this.size >= this.threshold)
		{
			// Rehash / flatten the table if the threshold is exceeded
			this.flatten();

			tab = this.entries;
			hash = hash(key);
			index = index(hash, tab.length);
		}

		tab[index] = new HashEntry<>(key, value, hash, tab[index]);
		this.size++;
	}

	@Override
	public void putAll(@NonNull Map<? extends K, ? extends V> map)
	{
		this.putAllInternal(map);
	}

	@Nullable
	@Override
	public V putIfAbsent(@Nullable K key, V value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				return e.value;
			}
		}

		this.addEntry(hash, key, value, i);
		return value;
	}

	@Override
	public boolean replace(@Nullable K key, V oldValue, V newValue)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				if (!Objects.equals(oldValue, newValue))
				{
					return false;
				}
				e.value = newValue;
				return true;
			}
		}

		return false;
	}

	@Nullable
	@Override
	public V replace(@Nullable K key, V newValue)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		for (HashEntry<K, V> e = this.entries[i]; e != null; e = e.next)
		{
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				V oldValue = e.value;
				e.value = newValue;
				return oldValue;
			}
		}

		return null;
	}

	@Nullable
	@Override
	public V removeKey(@Nullable Object key)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		HashEntry<K, V> prev = this.entries[i];
		HashEntry<K, V> e = prev;

		while (e != null)
		{
			HashEntry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				this.size--;
				if (prev == e)
				{
					this.entries[i] = next;
				}
				else
				{
					prev.next = next;
				}

				return e.value;
			}
			prev = e;
			e = next;
		}

		return null;
	}

	@Override
	public boolean removeValue(@Nullable Object value)
	{
		for (int i = 0; i < this.entries.length; i++)
		{
			HashEntry<K, V> prev = this.entries[i];
			HashEntry<K, V> e = prev;

			while (e != null)
			{
				HashEntry<K, V> next = e.next;
				Object v = e.value;
				if (v == value || value != null && value.equals(v))
				{
					this.size--;
					if (prev == e)
					{
						this.entries[i] = next;
					}
					else
					{
						prev.next = next;
					}

					return true;
				}
				prev = e;
				e = next;
			}
		}

		return false;
	}

	@Override
	public boolean remove(@Nullable Object key, Object value)
	{
		int hash = hash(key);
		int i = index(hash, this.entries.length);
		HashEntry<K, V> prev = this.entries[i];
		HashEntry<K, V> e = prev;

		while (e != null)
		{
			HashEntry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.key) == key || key != null && key.equals(k)))
			{
				if (!Objects.equals(value, e.value))
				{
					return false;
				}

				this.size--;
				if (prev == e)
				{
					this.entries[i] = next;
				}
				else
				{
					prev.next = next;
				}

				return true;
			}
			prev = e;
			e = next;
		}

		return false;
	}

	@Override
	public void mapValues(@NonNull BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		for (HashEntry<K, V> entry : this.entries)
		{
			while (entry != null)
			{
				entry.value = mapper.apply(entry.key, entry.value);
				entry = entry.next;
			}
		}
	}

	@Override
	public void filter(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		for (int i = 0; i < this.entries.length; i++)
		{
			HashEntry<K, V> prev = this.entries[i];
			HashEntry<K, V> e = prev;

			while (e != null)
			{
				HashEntry<K, V> next = e.next;
				if (!condition.test(e.key, e.value))
				{
					this.size--;
					if (prev == e)
					{
						this.entries[i] = next;
					}
					else
					{
						prev.next = next;
					}
				}
				prev = e;
				e = next;
			}
		}
	}

	@NonNull
	@Override
	public MutableMap<K, V> copy()
	{
		return this.mutableCopy();
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return this.immutableCopy();
	}
}

package dyvil.collection.mutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.lang.LiteralConvertible;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromNil
@LiteralConvertible.FromColonOperator(methodName = "singleton")
@LiteralConvertible.FromArray
public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = 5171722024919718041L;

	// Factory Methods

	public static <K, V> ArrayMap<K, V> singleton(K key, V value)
	{
		final ArrayMap<K, V> result = new ArrayMap<>();
		result.putInternal(key, value);
		return result;
	}

	public static <K, V> ArrayMap<K, V> apply()
	{
		return new ArrayMap<>();
	}

	@SafeVarargs
	public static <K, V> ArrayMap<K, V> apply(Entry<? extends K, ? extends V>... entries)
	{
		return new ArrayMap<>(entries);
	}

	public static <K, V> ArrayMap<K, V> from(Entry<? extends K, ? extends V>[] array)
	{
		return new ArrayMap<>(array);
	}

	public static <K, V> ArrayMap<K, V> from(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new ArrayMap<>(iterable);
	}

	public static <K, V> ArrayMap<K, V> from(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new ArrayMap<>(iterable);
	}

	public static <K, V> ArrayMap<K, V> from(Set<? extends Entry<? extends K, ? extends V>> set)
	{
		return new ArrayMap<>(set);
	}

	public static <K, V> ArrayMap<K, V> from(Map<? extends K, ? extends V> map)
	{
		return new ArrayMap<>(map);
	}

	public static <K, V> ArrayMap<K, V> from(AbstractArrayMap<? extends K, ? extends V> arrayMap)
	{
		return new ArrayMap<>(arrayMap);
	}

	// Constructors

	public ArrayMap()
	{
		super();
	}

	public ArrayMap(int capacity)
	{
		super(capacity);
	}

	public ArrayMap(K[] keys, V[] values)
	{
		super(keys, values);
	}

	public ArrayMap(K[] keys, V[] values, int size)
	{
		super(keys, values, size);
	}

	public ArrayMap(K[] keys, V[] values, boolean trusted)
	{
		super(keys, values, trusted);
	}

	public ArrayMap(K[] keys, V[] values, int size, boolean trusted)
	{
		super(keys, values, size, trusted);
	}

	public ArrayMap(Entry<? extends K, ? extends V>[] entries)
	{
		super(entries);
	}

	public ArrayMap(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public ArrayMap(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public ArrayMap(Set<? extends Entry<? extends K, ? extends V>> set)
	{
		super(set);
	}

	public ArrayMap(Map<? extends K, ? extends V> map)
	{
		super(map);
	}

	public ArrayMap(AbstractArrayMap<? extends K, ? extends V> arrayMap)
	{
		super(arrayMap);
	}

	// Implementation Methods

	@Override
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.keys[i] = this.values[i] = null;
		}
		this.size = 0;
	}

	@Override
	public Entry<K, V> getEntry(Object key)
	{
		final int index = this.getIndex(key);
		if (index < 0)
		{
			return null;
		}
		return new Entry<K, V>()
		{
			@Override
			public K getKey()
			{
				return (K) key;
			}

			@Override
			public V getValue()
			{
				return (V) ArrayMap.this.values[ArrayMap.this.getIndex(key)];
			}
		};
	}

	@Override
	public V put(K key, V value)
	{
		return this.putInternal(key, value);
	}

	@Override
	public V putIfAbsent(K key, V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				return (V) this.values[i];
			}
		}

		this.putNew(key, value);
		return value;
	}

	@Override
	public V replace(K key, V newValue)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				V oldValue = (V) this.values[i];
				this.values[i] = newValue;
				return oldValue;
			}
		}
		return null;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				if (!Objects.equals(oldValue, this.values[i]))
				{
					return false;
				}

				this.values[i] = newValue;
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
			System.arraycopy(this.keys, index + 1, this.keys, index, numMoved);
			System.arraycopy(this.values, index + 1, this.values, index, numMoved);
		}
		this.keys[this.size] = this.values[this.size] = null;
	}

	@Override
	public V removeKey(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				V oldValue = (V) this.values[i];
				this.removeAt(i);
				return oldValue;
			}
		}
		return null;
	}

	@Override
	public boolean removeValue(Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(value, this.values[i]))
			{
				this.removeAt(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean remove(Object key, Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				if (Objects.equals(value, this.values[i]))
				{
					this.removeAt(i);
					return true;
				}
				return false;
			}
		}
		return false;
	}

	@Override
	public void mapValues(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = mapper.apply((K) this.keys[i], (V) this.values[i]);
		}
	}

	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (!condition.test((K) this.keys[i], (V) this.values[i]))
			{
				this.removeAt(i--);
			}
		}
	}

	@Override
	public MutableMap<K, V> copy()
	{
		return this.mutableCopy();
	}

	@Override
	public ImmutableMap<K, V> immutable()
	{
		return this.immutableCopy();
	}
}

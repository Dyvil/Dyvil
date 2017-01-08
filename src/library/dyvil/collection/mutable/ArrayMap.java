package dyvil.collection.mutable;

import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
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

	@NonNull
	public static <K, V> ArrayMap<K, V> singleton(K key, V value)
	{
		final ArrayMap<K, V> result = new ArrayMap<>();
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> apply()
	{
		return new ArrayMap<>();
	}

	@NonNull
	@SafeVarargs
	public static <K, V> ArrayMap<K, V> apply(@NonNull Entry<? extends K, ? extends V>... entries)
	{
		return new ArrayMap<>(entries);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(Entry<? extends K, ? extends V> @NonNull [] array)
	{
		return new ArrayMap<>(array);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new ArrayMap<>(iterable);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new ArrayMap<>(iterable);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull Set<? extends Entry<? extends K, ? extends V>> set)
	{
		return new ArrayMap<>(set);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull Map<? extends K, ? extends V> map)
	{
		return new ArrayMap<>(map);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull AbstractArrayMap<? extends K, ? extends V> arrayMap)
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

	public ArrayMap(K @NonNull [] keys, V @NonNull [] values)
	{
		super(keys, values);
	}

	public ArrayMap(K @NonNull [] keys, V @NonNull [] values, int size)
	{
		super(keys, values, size);
	}

	public ArrayMap(K @NonNull [] keys, V @NonNull [] values, boolean trusted)
	{
		super(keys, values, trusted);
	}

	public ArrayMap(K[] keys, V[] values, int size, boolean trusted)
	{
		super(keys, values, size, trusted);
	}

	public ArrayMap(Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public ArrayMap(@NonNull Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public ArrayMap(@NonNull SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public ArrayMap(@NonNull Set<? extends Entry<? extends K, ? extends V>> set)
	{
		super(set);
	}

	public ArrayMap(@NonNull Map<? extends K, ? extends V> map)
	{
		super(map);
	}

	public ArrayMap(@NonNull AbstractArrayMap<? extends K, ? extends V> arrayMap)
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

	@Nullable
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
				return (V) ArrayMap.this.values[ArrayMap.this.getIndex(key)];
			}
		};
	}

	@Nullable
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

	@Nullable
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

	@Nullable
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
	public void mapValues(@NonNull BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		for (int i = 0; i < this.size; i++)
		{
			this.values[i] = mapper.apply((K) this.keys[i], (V) this.values[i]);
		}
	}

	@Override
	public void filter(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (!condition.test((K) this.keys[i], (V) this.values[i]))
			{
				this.removeAt(i--);
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

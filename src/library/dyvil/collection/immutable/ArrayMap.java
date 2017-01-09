package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@SuppressWarnings("SuspiciousSystemArraycopy")
@LiteralConvertible.FromNil
@LiteralConvertible.FromArray
@LiteralConvertible.FromColonOperator(methodName = "singleton")
@Immutable
public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements ImmutableMap<K, V>
{
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private ArrayMap<K, V> map;

		public Builder()
		{
			this.map = new ArrayMap<>();
		}

		public Builder(int capacity)
		{
			this.map = new ArrayMap<>(capacity);
		}

		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built");
			}

			this.map.putInternal(key, value);
		}

		@Override
		public ArrayMap<K, V> build()
		{
			ArrayMap<K, V> map = this.map;
			this.map = null;
			return map;
		}
	}

	private static final long serialVersionUID = 4583062458335627011L;

	// Factory Methods

	@NonNull
	public static <K, V> ArrayMap<K, V> singleton(K key, V value)
	{
		final ArrayMap<K, V> result = new ArrayMap<>(1);
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> apply()
	{
		return new ArrayMap<>(0);
	}

	@NonNull
	@SafeVarargs
	public static <K, V> ArrayMap<K, V> apply(@NonNull Entry<? extends K, ? extends V>... entries)
	{
		return new ArrayMap<>(entries);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull Entry<? extends K, ? extends V> @NonNull [] array)
	{
		return new ArrayMap<>(array);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		return new ArrayMap<>(iterable);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull SizedIterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		return new ArrayMap<>(iterable);
	}

	@NonNull
	public static <K, V> ArrayMap<K, V> from(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
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

	@NonNull
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<>();
	}

	@NonNull
	public static <K, V> Builder<K, V> builder(int capacity)
	{
		return new Builder<>(capacity);
	}

	// Constructors

	protected ArrayMap()
	{
		super();
	}

	protected ArrayMap(int capacity)
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

	public ArrayMap(K @NonNull [] keys, V @NonNull [] values, int size, boolean trusted)
	{
		super(keys, values, size, trusted);
	}

	public ArrayMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public ArrayMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public ArrayMap(@NonNull SizedIterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public ArrayMap(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
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
	protected void removeAt(int index)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
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

		return new Tuple.Of2<>((K) key, (V) this.values[index]);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		ArrayMap<K, V> copy = new ArrayMap<>(this);
		copy.putInternal(key, value);
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		ArrayMap<K, V> copy = new ArrayMap<>(this.size + map.size());

		// Copy our keys and values
		System.arraycopy(this.keys, 0, copy.keys, 0, this.size);
		System.arraycopy(this.values, 0, copy.values, 0, this.size);

		// Put the new keys and values
		for (Entry<? extends K, ? extends V> entry : map)
		{
			copy.putInternal(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		K[] keys = (K[]) new Object[this.size];
		V[] values = (V[]) new Object[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (Objects.equals(key, k))
			{
				continue;
			}

			keys[index] = k;
			values[index++] = (V) this.values[i];
		}
		return new ArrayMap<>(keys, values, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		K[] keys = (K[]) new Object[this.size];
		V[] values = (V[]) new Object[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (Objects.equals(key, k))
			{
				continue;
			}
			V v = (V) this.values[i];
			if (Objects.equals(value, v))
			{
				continue;
			}
			keys[index] = k;
			values[index++] = v;
		}
		return new ArrayMap<>(keys, values, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		K[] keys = (K[]) new Object[this.size];
		V[] values = (V[]) new Object[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			V v = (V) this.values[i];
			if (Objects.equals(value, v))
			{
				continue;
			}

			keys[index] = (K) this.keys[i];
			values[index++] = v;
		}
		return new ArrayMap<>(keys, values, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		K[] keys = (K[]) new Object[this.size];
		V[] values = (V[]) new Object[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			V v = (V) this.values[i];
			if (map.contains(k, v))
			{
				continue;
			}

			keys[index] = k;
			values[index++] = v;
		}
		return new ArrayMap<>(keys, values, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> collection)
	{
		K[] keys = (K[]) new Object[this.size];
		V[] values = (V[]) new Object[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (collection.contains(k))
			{
				continue;
			}

			keys[index] = k;
			values[index++] = (V) this.values[i];
		}
		return new ArrayMap<>(keys, values, index, true);
	}

	@NonNull
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		ArrayMap<NK, V> copy = new ArrayMap<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			V value = (V) this.values[i];
			copy.putInternal(mapper.apply((K) this.keys[i], value), value);
		}
		return copy;
	}

	@NonNull
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		K[] keys = (K[]) new Object[this.size];
		NV[] values = (NV[]) new Object[this.size];

		System.arraycopy(this.keys, 0, keys, 0, this.size);
		for (int i = 0; i < this.size; i++)
		{
			values[i] = mapper.apply((K) this.keys[i], (V) this.values[i]);
		}
		return new ArrayMap<>(keys, values, this.size, true);
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		ArrayMap<NK, NV> copy = new ArrayMap<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			Entry<? extends NK, ? extends NV> entry = mapper.apply((K) this.keys[i], (V) this.values[i]);
			if (entry == null)
			{
				continue;
			}

			copy.putInternal(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		ArrayMap<NK, NV> copy = new ArrayMap<>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			for (Entry<? extends NK, ? extends NV> entry : mapper.apply((K) this.keys[i], (V) this.values[i]))
			{
				copy.putInternal(entry.getKey(), entry.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		K[] keys = (K[]) new Object[this.size];
		V[] values = (V[]) new Object[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			V v = (V) this.values[i];
			if (!condition.test(k, v))
			{
				continue;
			}

			keys[index] = k;
			values[index] = v;
			index++;
		}
		return new ArrayMap<>(keys, values, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		V[] keys = (V[]) new Object[this.size];
		K[] values = (K[]) new Object[this.size];
		System.arraycopy(this.keys, 0, values, 0, this.size);
		System.arraycopy(this.values, 0, keys, 0, this.size);
		return new ArrayMap<>(keys, values, this.size, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this.immutableCopy();
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public java.util.@NonNull Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

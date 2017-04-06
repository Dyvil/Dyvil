package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractIdentityHashMap;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromArray
@Immutable
public class IdentityHashMap<K, V> extends AbstractIdentityHashMap<K, V> implements ImmutableMap<K, V>
{
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private IdentityHashMap<K, V> map;

		public Builder()
		{
			this.map = new IdentityHashMap<>();
		}

		public Builder(int capacity)
		{
			this.map = new IdentityHashMap<>(capacity);
		}

		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built!");
			}

			this.map.putInternal(key, value);
		}

		@Override
		public IdentityHashMap<K, V> build()
		{
			IdentityHashMap<K, V> map = this.map;
			this.map = null;
			return map;
		}
	}

	private static final long serialVersionUID = 7106880090218416170L;

	// Factory Methods

	@NonNull
	public static <K, V> IdentityHashMap<K, V> singleton(K key, V value)
	{
		final IdentityHashMap<K, V> result = new IdentityHashMap<>(1);
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	@SafeVarargs
	public static <K, V> IdentityHashMap<K, V> apply(@NonNull Entry<? extends K, ? extends V> @NonNull ... entries)
	{
		return new IdentityHashMap<>(entries);
	}

	@NonNull
	public static <K, V> IdentityHashMap<K, V> from(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		return new IdentityHashMap<>(entries);
	}

	@NonNull
	public static <K, V> IdentityHashMap<K, V> from(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		return new IdentityHashMap<>(iterable);
	}

	@NonNull
	public static <K, V> IdentityHashMap<K, V> from(SizedIterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		return new IdentityHashMap<>(iterable);
	}

	@NonNull
	public static <K, V> IdentityHashMap<K, V> from(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
	{
		return new IdentityHashMap<>(set);
	}

	@NonNull
	public static <K, V> IdentityHashMap<K, V> from(@NonNull Map<? extends K, ? extends V> map)
	{
		return new IdentityHashMap<>(map);
	}

	@NonNull
	public static <K, V> IdentityHashMap<K, V> from(@NonNull AbstractIdentityHashMap<? extends K, ? extends V> identityHashMap)
	{
		return new IdentityHashMap<>(identityHashMap);
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

	protected IdentityHashMap()
	{
		super();
	}

	protected IdentityHashMap(int capacity)
	{
		super(capacity);
	}

	public IdentityHashMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public IdentityHashMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public IdentityHashMap(SizedIterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public IdentityHashMap(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
	{
		super(set);
	}

	public IdentityHashMap(@NonNull Map<? extends K, ? extends V> map)
	{
		super(map);
	}

	public IdentityHashMap(@NonNull AbstractIdentityHashMap<? extends K, ? extends V> identityHashMap)
	{
		super(identityHashMap);
	}

	// Implementation Methods

	@Nullable
	@Override
	public Entry<K, V> getEntry(Object key)
	{
		final int index = this.getIndex(key);
		if (index < 0)
		{
			return null;
		}
		return new Tuple.Of2<>((K) key, (V) this.table[index + 1]);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		IdentityHashMap<K, V> map = new IdentityHashMap<>(this);
		map.ensureCapacity(this.size + 1);
		map.putInternal(key, value);
		return map;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		final IdentityHashMap<K, V> copy = new IdentityHashMap<>(this);
		copy.putAllInternal(map);
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		final IdentityHashMap<K, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			final K entryKey = entry.getKey();
			if (entryKey != key)
			{
				copy.putInternal(entryKey, entry.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (entryKey != key && entryValue != value)
			{
				copy.putInternal(entryKey, entryValue);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			V v = entry.getValue();
			if (v != value)
			{
				copy.putInternal(entry.getKey(), v);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!map.contains(entryKey, entryValue))
			{
				copy.putInternal(entryKey, entryValue);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			K entryKey = entry.getKey();
			if (!keys.contains(entryKey))
			{
				copy.putInternal(entryKey, entry.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		IdentityHashMap<NK, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			V value = entry.getValue();
			copy.putInternal(mapper.apply(entry.getKey(), value), value);
		}
		return copy;
	}

	@NonNull
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		IdentityHashMap<K, NV> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			K key = entry.getKey();
			copy.putInternal(key, mapper.apply(key, entry.getValue()));
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		IdentityHashMap<NK, NV> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			Entry<? extends NK, ? extends NV> result = mapper.apply(entry.getKey(), entry.getValue());
			if (result != null)
			{
				copy.putInternal(result.getKey(), result.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		IdentityHashMap<NK, NV> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends NK, ? extends NV> result : mapper.apply(entry.getKey(), entry.getValue()))
			{
				copy.putInternal(result.getKey(), result.getValue());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			K key = entry.getKey();
			V value = entry.getValue();
			if (condition.test(key, value))
			{
				copy.putInternal(key, value);
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		IdentityHashMap<V, K> copy = new IdentityHashMap<>(this.size);
		for (Entry<K, V> entry : this)
		{
			copy.putInternal(entry.getValue(), entry.getKey());
		}
		return copy;
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
}

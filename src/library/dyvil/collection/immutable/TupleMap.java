package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractTupleMap;
import dyvil.lang.LiteralConvertible;
import dyvil.tuple.Tuple;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromArray
@Immutable
public class TupleMap<K, V> extends AbstractTupleMap<K, V> implements ImmutableMap<K, V>
{
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private TupleMap<K, V> map;

		public Builder()
		{
			this.map = new TupleMap<>(DEFAULT_CAPACITY);
		}

		public Builder(int capacity)
		{
			this.map = new TupleMap<>(capacity);
		}

		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built");
			}

			this.map.putInternal(new Tuple.Of2<>(key, value));
		}

		@Override
		public void put(@NonNull Entry<? extends K, ? extends V> entry)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built");
			}

			this.map.putInternal((Tuple.Of2<K, V>) entry.toTuple());
		}

		@Override
		public TupleMap<K, V> build()
		{
			final TupleMap<K, V> map = this.map;
			this.map = null;
			return map;
		}
	}

	private static final long serialVersionUID = -5372836862143742212L;

	// Factory Methods

	@NonNull
	public static <K, V> TupleMap<K, V> singleton(K key, V value)
	{
		return apply(new Tuple.Of2<>(key, value));
	}

	@NonNull
	public static <K, V> TupleMap<K, V> apply()
	{
		return new TupleMap<>(0);
	}

	@NonNull
	@SafeVarargs
	public static <K, V> TupleMap<K, V> apply(@NonNull Entry<? extends K, ? extends V> @NonNull ... entries)
	{
		return new TupleMap<>(entries);
	}

	@NonNull
	@SafeVarargs
	public static <K, V> TupleMap<K, V> apply(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull ... entries)
	{
		return new TupleMap<>(entries, true);
	}

	@NonNull
	public static <K, V> TupleMap<K, V> from(@NonNull Entry<? extends K, ? extends V> @NonNull [] array)
	{
		return new TupleMap<>(array);
	}

	@NonNull
	public static <K, V> TupleMap<K, V> from(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] array)
	{
		return new TupleMap<>(array);
	}

	@NonNull
	public static <K, V> TupleMap<K, V> from(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
	{
		return new TupleMap<>(set);
	}

	@NonNull
	public static <K, V> TupleMap<K, V> from(@NonNull Map<? extends K, ? extends V> map)
	{
		return new TupleMap<>(map);
	}

	@NonNull
	public static <K, V> TupleMap<K, V> from(@NonNull AbstractTupleMap<? extends K, ? extends V> tupleMap)
	{
		return new TupleMap<>(tupleMap);
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

	protected TupleMap(int capacity)
	{
		super(capacity);
	}

	public TupleMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public TupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public TupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries, int size)
	{
		super(entries, size);
	}

	public TupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries, boolean trusted)
	{
		super(entries, trusted);
	}

	public TupleMap(Tuple.@NonNull Of2<? extends K, ? extends V> @NonNull [] entries, int size, boolean trusted)
	{
		super(entries, size, trusted);
	}

	public TupleMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public TupleMap(@NonNull SizedIterable<? extends @NonNull Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public TupleMap(@NonNull Set<? extends @NonNull Entry<? extends K, ? extends V>> set)
	{
		super(set);
	}

	public TupleMap(@NonNull Map<? extends K, ? extends V> map)
	{
		super(map);
	}

	public TupleMap(@NonNull AbstractTupleMap<? extends K, ? extends V> map)
	{
		super(map);
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
		return new Tuple.Of2<>((K) key, this.entries[index]._2);
	}

	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		TupleMap<K, V> copy = new TupleMap<>(this);
		copy.putInternal(new Tuple.Of2<>(key, value));
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		TupleMap<K, V> copy = new TupleMap<>(this);
		for (Entry<? extends K, ? extends V> entry : map)
		{
			copy.putInternal((Tuple.Of2<K, V>) entry.toTuple());
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		Tuple.Of2<K, V>[] entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				continue;
			}

			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		Tuple.Of2<K, V>[] entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1) && Objects.equals(value, entry._2))
			{
				continue;
			}

			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		Tuple.Of2<K, V>[] entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (Objects.equals(value, entry._2))
			{
				continue;
			}

			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		Tuple.Of2<K, V>[] entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (map.contains(entry))
			{
				continue;
			}

			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
	{
		Tuple.Of2<K, V>[] entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (keys.contains(entry._1))
			{
				continue;
			}

			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}

	@NonNull
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		int len = this.size;
		TupleMap<NK, V> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			V value = entry._2;
			copy.putInternal(new Tuple.Of2<>(mapper.apply(entry._1, value), value));
		}
		return copy;
	}

	@NonNull
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		Tuple.Of2<K, NV>[] entries = (Tuple.Of2<K, NV>[]) new Tuple.Of2[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			K key = entry._1;
			entries[i] = new Tuple.Of2<>(key, mapper.apply(key, entry._2));
		}
		return new TupleMap<>(entries, this.size, true);
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		int len = this.size;
		TupleMap<NK, NV> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			Entry<? extends NK, ? extends NV> newEntry = mapper.apply(entry._1, entry._2);
			if (newEntry != null)
			{
				copy.putInternal((Tuple.Of2<NK, NV>) newEntry.toTuple());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		int len = this.size;
		TupleMap<NK, NV> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			for (Entry<? extends NK, ? extends NV> newEntry : mapper.apply(entry._1, entry._2))
			{
				copy.putInternal((Tuple.Of2<NK, NV>) newEntry.toTuple());
			}
		}
		return copy;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> predicate)
	{
		Tuple.Of2<K, V>[] entries = (Tuple.Of2<K, V>[]) new Tuple.Of2[this.size];

		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			if (predicate.test(entry._1, entry._2))
			{
				entries[index++] = entry;
			}
		}
		return new TupleMap<>(entries, index, true);
	}

	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		int len = this.size;
		TupleMap<V, K> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple.Of2<K, V> entry = this.entries[i];
			copy.putInternal(new Tuple.Of2<>(entry._2, entry._1));
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

	@Override
	public java.util.@NonNull Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

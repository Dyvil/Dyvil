package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractTupleMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.ColonConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@NilConvertible
@ArrayConvertible
@ColonConvertible
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

			this.map.putInternal(new Tuple2<>(key, value));
		}

		@Override
		public void put(Entry<? extends K, ? extends V> entry)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built");
			}

			this.map.putInternal((Tuple2<K, V>) entry.toTuple());
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

	public static <K, V> TupleMap<K, V> apply(K key, V value)
	{
		return new TupleMap<>(new Tuple2<>(key, value));
	}

	@SafeVarargs
	public static <K, V> TupleMap<K, V> apply(Entry<K, V>... entries)
	{
		return new TupleMap<>(entries);
	}

	@SafeVarargs
	public static <K, V> TupleMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new TupleMap<>(entries, true);
	}

	public static <K, V> TupleMap<K, V> fromArray(Tuple2<K, V>[] entries)
	{
		return new TupleMap<>(entries);
	}

	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<>();
	}

	public static <K, V> Builder<K, V> builder(int capacity)
	{
		return new Builder<>(capacity);
	}

	protected TupleMap(int capacity)
	{
		super(capacity);
	}

	@SafeVarargs
	public TupleMap(Entry<K, V>... entries)
	{
		super(entries);
	}

	@SafeVarargs
	public TupleMap(Tuple2<K, V>... entries)
	{
		super(entries);
	}

	public TupleMap(Tuple2<K, V>[] entries, int size)
	{
		super(entries, size);
	}

	public TupleMap(Tuple2<K, V>[] entries, boolean trusted)
	{
		super(entries, trusted);
	}

	public TupleMap(Tuple2<K, V>[] entries, int size, boolean trusted)
	{
		super(entries, size, trusted);
	}

	public TupleMap(Map<K, V> map)
	{
		super(map);
	}

	public TupleMap(AbstractTupleMap<K, V> map)
	{
		super(map);
	}

	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
	}
	
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		TupleMap<K, V> copy = new TupleMap<>(this);
		copy.putInternal(new Tuple2<>(key, value));
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> union(Map<? extends K, ? extends V> map)
	{
		TupleMap<K, V> copy = new TupleMap<>(this);
		for (Entry<? extends K, ? extends V> entry : map)
		{
			copy.putInternal((Tuple2<K, V>) entry.toTuple());
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		Tuple2<K, V>[] entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		Tuple2<K, V>[] entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1) && Objects.equals(value, entry._2))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		Tuple2<K, V>[] entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(value, entry._2))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> difference(Map<?, ?> map)
	{
		Tuple2<K, V>[] entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (map.contains(entry))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> keyDifference(Collection<?> keys)
	{
		Tuple2<K, V>[] entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (keys.contains(entry._1))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap<>(entries, index, true);
	}
	
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		int len = this.size;
		TupleMap<NK, V> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			V value = entry._2;
			copy.putInternal(new Tuple2<>(mapper.apply(entry._1, value), value));
		}
		return copy;
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		Tuple2<K, NV>[] entries = (Tuple2<K, NV>[]) new Tuple2[this.size];
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			K key = entry._1;
			entries[i] = new Tuple2<>(key, mapper.apply(key, entry._2));
		}
		return new TupleMap<>(entries, this.size, true);
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		int len = this.size;
		TupleMap<NK, NV> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			Entry<? extends NK, ? extends NV> newEntry = mapper.apply(entry._1, entry._2);
			if (newEntry != null)
			{
				copy.putInternal((Tuple2<NK, NV>) newEntry.toTuple());
			}
		}
		return copy;
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		int len = this.size;
		TupleMap<NK, NV> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			for (Entry<? extends NK, ? extends NV> newEntry : mapper.apply(entry._1, entry._2))
			{
				copy.putInternal((Tuple2<NK, NV>) newEntry.toTuple());
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Tuple2<K, V>[] entries = (Tuple2<K, V>[]) new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (condition.test(entry._1, entry._2))
			{
				entries[index++] = entry;
			}
		}
		return new TupleMap<>(entries, index, true);
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		int len = this.size;
		TupleMap<V, K> copy = new TupleMap<>(len);
		for (int i = 0; i < len; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			copy.putInternal(new Tuple2<>(entry._2, entry._1));
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this.immutableCopy();
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

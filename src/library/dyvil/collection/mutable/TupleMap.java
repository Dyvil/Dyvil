package dyvil.collection.mutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractTupleMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.ColonConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@NilConvertible
@ColonConvertible(methodName = "singleton")
@ArrayConvertible
public class TupleMap<K, V> extends AbstractTupleMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = 5771226814337471265L;

	// Factory Methods

	public static <K, V> TupleMap<K, V> singleton(K key, V value)
	{
		final TupleMap<K, V> result = new TupleMap<>();
		result.putInternal(new Tuple2<>(key, value));
		return result;
	}

	public static <K, V> TupleMap<K, V> apply()
	{
		return new TupleMap<>();
	}

	@SafeVarargs
	public static <K, V> TupleMap<K, V> apply(Entry<? extends K, ? extends V>... entries)
	{
		return new TupleMap<>(entries);
	}

	@SafeVarargs
	public static <K, V> TupleMap<K, V> apply(Tuple2<? extends K, ? extends V>... entries)
	{
		return new TupleMap<>(entries, true);
	}

	public static <K, V> TupleMap<K, V> from(Entry<? extends K, ? extends V>[] array)
	{
		return new TupleMap<>(array);
	}

	public static <K, V> TupleMap<K, V> from(Tuple2<? extends K, ? extends V>[] array)
	{
		return new TupleMap<>(array);
	}

	public static <K, V> TupleMap<K, V> from(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new TupleMap<>(iterable);
	}

	public static <K, V> TupleMap<K, V> from(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		return new TupleMap<>(iterable);
	}

	public static <K, V> TupleMap<K, V> from(Set<? extends Entry<? extends K, ? extends V>> set)
	{
		return new TupleMap<>(set);
	}

	public static <K, V> TupleMap<K, V> from(Map<? extends K, ? extends V> map)
	{
		return new TupleMap<>(map);
	}

	public static <K, V> TupleMap<K, V> from(AbstractTupleMap<? extends K, ? extends V> tupleMap)
	{
		return new TupleMap<>(tupleMap);
	}

	// Constructors

	public TupleMap()
	{
		super();
	}

	public TupleMap(int capacity)
	{
		super(capacity);
	}

	public TupleMap(Entry<? extends K, ? extends V>[] entries)
	{
		super(entries);
	}

	public TupleMap(Tuple2<? extends K, ? extends V>[] entries)
	{
		super(entries);
	}

	public TupleMap(Tuple2<? extends K, ? extends V>[] entries, int size)
	{
		super(entries, size);
	}

	public TupleMap(Tuple2<? extends K, ? extends V>[] entries, boolean trusted)
	{
		super(entries, trusted);
	}

	public TupleMap(Tuple2<? extends K, ? extends V>[] entries, int size, boolean trusted)
	{
		super(entries, size, trusted);
	}

	public TupleMap(Iterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public TupleMap(SizedIterable<? extends Entry<? extends K, ? extends V>> iterable)
	{
		super(iterable);
	}

	public TupleMap(Set<? extends Entry<? extends K, ? extends V>> set)
	{
		super(set);
	}

	public TupleMap(Map<? extends K, ? extends V> map)
	{
		super(map);
	}

	public TupleMap(AbstractTupleMap<? extends K, ? extends V> map)
	{
		super(map);
	}

	// Implementation Methods

	@Override
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.entries[i] = null;
		}
		this.size = 0;
	}

	@Override
	public V put(K key, V value)
	{
		return this.put(new Tuple2<>(key, value));
	}

	@Override
	public V put(Entry<? extends K, ? extends V> entry)
	{
		return this.putInternal((Tuple2<K, V>) entry.toTuple());
	}

	@Override
	public V putIfAbsent(K key, V value)
	{
		return this.putIfAbsent(new Tuple2<>(key, value));
	}

	@Override
	public V putIfAbsent(Entry<? extends K, ? extends V> entry)
	{
		return this.putIfAbsent((Tuple2<K, V>) entry.toTuple());
	}

	private V putIfAbsent(Tuple2<K, V> tuple)
	{
		final K key = tuple._1;
		for (int i = 0; i < this.size; i++)
		{
			final Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				return entry._2;
			}
		}

		this.putNew(tuple);
		return tuple._2;
	}

	@Override
	public V replace(K key, V newValue)
	{
		return this.replace(new Tuple2<>(key, newValue));
	}

	@Override
	public V replace(Entry<? extends K, ? extends V> entry)
	{
		return this.replace((Tuple2<K, V>) entry.toTuple());
	}

	private V replace(Tuple2<K, V> tuple)
	{
		K key = tuple._1;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				V oldValue = entry._2;
				this.entries[i] = tuple;
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
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				if (!Objects.equals(oldValue, entry._2))
				{
					return false;
				}

				this.entries[i] = new Tuple2<>(key, newValue);
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
			System.arraycopy(this.entries, index + 1, this.entries, index, numMoved);
		}
		this.entries[this.size] = null;
	}

	@Override
	public V removeKey(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				V oldValue = entry._2;
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
			if (Objects.equals(value, this.entries[i]._2))
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
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				if (Objects.equals(value, entry._2))
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
			Tuple2<K, V> entry = this.entries[i];
			K key = entry._1;
			this.entries[i] = new Tuple2<>(key, mapper.apply(key, entry._2));
		}
	}

	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (!condition.test(entry._1, entry._2))
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

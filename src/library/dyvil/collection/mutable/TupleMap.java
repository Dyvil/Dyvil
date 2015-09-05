package dyvil.collection.mutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractTupleMap;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public class TupleMap<K, V> extends AbstractTupleMap<K, V>implements MutableMap<K, V>
{
	protected static final int DEFAULT_CAPACITY = 10;
	
	public static <K, V> TupleMap<K, V> apply()
	{
		return new TupleMap(DEFAULT_CAPACITY);
	}
	
	public static <K, V> TupleMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new TupleMap(entries, true);
	}
	
	public static <K, V> TupleMap<K, V> fromArray(Tuple2<K, V>[] entries)
	{
		return new TupleMap(entries);
	}
	
	public TupleMap()
	{
		super(new Tuple2[DEFAULT_CAPACITY], 0);
	}
	
	public TupleMap(int capacity)
	{
		super(new Tuple2[capacity], 0);
	}
	
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
	
	@Override
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.entries[i] = null;
		}
		this.size = 0;
	}
	
	private void putNew(Tuple2<K, V> tuple)
	{
		int index = this.size++;
		if (index >= this.entries.length)
		{
			int newCapacity = (int) (this.size * 1.1F);
			Tuple2[] newEntries = new Tuple2[newCapacity];
			System.arraycopy(this.entries, 0, newEntries, 0, index);
			this.entries = newEntries;
		}
		this.entries[index] = tuple;
	}
	
	@Override
	public V put(K key, V value)
	{
		return this.put(new Tuple2<K, V>(key, value));
	}
	
	@Override
	public V put(Entry<? extends K, ? extends V> entry)
	{
		return this.put(entry.toTuple());
	}
	
	private V put(Tuple2<K, V> tuple)
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
		
		this.putNew(tuple);
		return null;
	}
	
	@Override
	public boolean putIfAbsent(K key, V value)
	{
		return this.putIfAbsent(new Tuple2<K, V>(key, value));
	}
	
	@Override
	public boolean putIfAbsent(Entry<? extends K, ? extends V> entry)
	{
		return this.putIfAbsent(entry.toTuple());
	}
	
	private boolean putIfAbsent(Tuple2<K, V> tuple)
	{
		K key = tuple._1;
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.entries[i]._1))
			{
				return false;
			}
		}
		
		this.putNew(tuple);
		return true;
	}
	
	@Override
	public V replace(K key, V newValue)
	{
		return this.replace(new Tuple2<K, V>(key, newValue));
	}
	
	@Override
	public V replace(Entry<? extends K, ? extends V> entry)
	{
		return this.replace(entry.toTuple());
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
				
				this.entries[i] = new Tuple2<K, V>(key, newValue);
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
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			K key = entry._1;
			this.entries[i] = new Tuple2(key, mapper.apply(key, entry._2));
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
		return new TupleMap<K, V>(this.entries, this.size);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new TupleMap<RK, RV>();
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new dyvil.collection.immutable.TupleMap<>(this.entries, this.size);
	}
}

package dyvil.collection.mutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.tuple.Tuple2;

@NilConvertible
@ArrayConvertible
public class ArrayMap<K, V> extends AbstractArrayMap<K, V>implements MutableMap<K, V>
{
	public static <K, V> ArrayMap<K, V> apply()
	{
		return new ArrayMap<K, V>(DEFAULT_CAPACITY);
	}
	
	public static <K, V> ArrayMap<K, V> apply(Tuple2<K, V>... entries)
	{
		int len = entries.length;
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		int size = AbstractArrayMap.fillEntries(keys, values, entries, len);
		return new ArrayMap<K, V>(keys, values, size, true);
	}
	
	public ArrayMap()
	{
		super(new Object[DEFAULT_CAPACITY], new Object[DEFAULT_CAPACITY], 0, true);
	}
	
	public ArrayMap(int capacity)
	{
		super(new Object[capacity], new Object[capacity], 0, true);
	}
	
	public ArrayMap(K[] keys, V[] values)
	{
		super(keys, values);
	}
	
	public ArrayMap(K[] keys, V[] values, int size)
	{
		super(keys, values);
	}
	
	public ArrayMap(Object[] keys, Object[] values, boolean trusted)
	{
		super(keys, values, keys.length, trusted);
	}
	
	public ArrayMap(Object[] keys, Object[] values, int size, boolean trusted)
	{
		super(keys, values, size, trusted);
	}
	
	public ArrayMap(Map<K, V> map)
	{
		super(map);
	}
	
	@Override
	public void clear()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.keys[i] = this.values[i] = null;
		}
		this.size = 0;
	}
	
	private void putNew(K key, V value)
	{
		int index = this.size++;
		if (index >= this.keys.length)
		{
			int newCapacity = (int) (this.size * 1.1F);
			Object[] newKeys = new Object[newCapacity];
			Object[] newValues = new Object[newCapacity];
			System.arraycopy(this.keys, 0, newKeys, 0, index);
			System.arraycopy(this.values, 0, newValues, 0, newCapacity);
			this.keys = newKeys;
			this.values = newValues;
		}
		this.keys[index] = key;
		this.values[index] = value;
	}
	
	@Override
	public V put(K key, V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				V oldValue = (V) this.values[i];
				this.values[i] = value;
				return oldValue;
			}
		}
		
		this.putNew(key, value);
		return null;
	}
	
	@Override
	public boolean putIfAbsent(K key, V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				return false;
			}
		}
		
		this.putNew(key, value);
		return true;
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
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
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
		return new ArrayMap(this.keys, this.values, this.size);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new ArrayMap();
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new dyvil.collection.immutable.ArrayMap(this.keys, this.values, this.size);
	}
	
	public ImmutableMap<K, V> trustedImmutable()
	{
		return new dyvil.collection.immutable.ArrayMap(this.keys, this.values, this.size, true);
	}
}

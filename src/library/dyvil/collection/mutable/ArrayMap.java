package dyvil.collection.mutable;

import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@NilConvertible
@ArrayConvertible
public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = 5171722024919718041L;
	
	public static <K, V> ArrayMap<K, V> apply()
	{
		return new ArrayMap<>(DEFAULT_CAPACITY);
	}
	
	@SafeVarargs
	public static <K, V> ArrayMap<K, V> apply(Entry<K, V>... entries)
	{
		return new ArrayMap<>(entries);
	}
	
	public ArrayMap()
	{
		super(DEFAULT_CAPACITY);
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
	
	public ArrayMap(AbstractArrayMap<K, V> map)
	{
		super(map);
	}
	
	@SafeVarargs
	public ArrayMap(Entry<K, V>... entries)
	{
		super(entries);
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
	
	@Override
	public V put(K key, V value)
	{
		return this.putInternal(key, value);
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

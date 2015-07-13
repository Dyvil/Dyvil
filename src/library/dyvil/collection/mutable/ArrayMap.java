package dyvil.collection.mutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.array.ObjectArray;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractArrayMap;

public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements MutableMap<K, V>
{
	protected static final int	DEFAULT_CAPACITY	= 10;
	
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
		this.size = 0;
		this.keys = ObjectArray.EMPTY;
		this.values = ObjectArray.EMPTY;
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
		
		return null;
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

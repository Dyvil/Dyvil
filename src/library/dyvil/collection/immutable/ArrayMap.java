package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.mutable.HashMap;
import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.tuple.Tuple2;

public class ArrayMap<K, V> implements ImmutableMap<K, V>
{
	protected class ArrayEntry implements Entry<K, V>
	{
		private int	index;
		
		private ArrayEntry(int index)
		{
			this.index = index;
		}
		
		@Override
		public K getKey()
		{
			return ArrayMap.this.keys[this.index];
		}
		
		@Override
		public V getValue()
		{
			return ArrayMap.this.values[this.index];
		}
	}
	
	private final int	size;
	private final K[]	keys;
	private final V[]	values;
	
	public ArrayMap(K[] keys, V[] values)
	{
		int size = keys.length;
		if (size != values.length)
		{
			throw new IllegalArgumentException("keys.length != values.length");
		}
		
		this.keys = (K[]) new Object[size];
		System.arraycopy(keys, 0, this.keys, 0, size);
		this.values = (V[]) new Object[size];
		System.arraycopy(values, 0, this.values, 0, size);
		this.size = size;
	}
	
	public ArrayMap(K[] keys, V[] values, int size)
	{
		if (keys.length < size)
		{
			throw new IllegalArgumentException("keys.length < size");
		}
		if (values.length < size)
		{
			throw new IllegalArgumentException("values.length < size");
		}
		
		this.keys = (K[]) new Object[size];
		System.arraycopy(keys, 0, this.keys, 0, size);
		this.values = (V[]) new Object[size];
		System.arraycopy(values, 0, this.values, 0, size);
		this.size = size;
	}
	
	public ArrayMap(K[] keys, V[] values, boolean trusted)
	{
		this.keys = keys;
		this.values = values;
		this.size = keys.length;
	}
	
	public ArrayMap(K[] keys, V[] values, int size, boolean trusted)
	{
		this.keys = keys;
		this.values = values;
		this.size = size;
	}
	
	public ArrayMap(Map<K, V> map)
	{
		this.size = map.size();
		this.keys = (K[]) new Object[this.size];
		this.values = (V[]) new Object[this.size];
		
		int index = 0;
		for (Entry<K, V> entry : map)
		{
			this.keys[index] = entry.getKey();
			this.values[index] = entry.getValue();
			index++;
		}
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new Iterator<Entry<K, V>>()
		{
			private int	index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < ArrayMap.this.size;
			}
			
			@Override
			public Entry<K, V> next()
			{
				return new ArrayEntry(this.index++);
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new Iterator<K>()
		{
			private int	index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < ArrayMap.this.size;
			}
			
			@Override
			public K next()
			{
				return ArrayMap.this.keys[this.index++];
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new Iterator<V>()
		{
			private int	index;
			
			@Override
			public boolean hasNext()
			{
				return this.index < ArrayMap.this.size;
			}
			
			@Override
			public V next()
			{
				return ArrayMap.this.values[this.index++];
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(new Tuple2(this.keys[i], this.values[i]));
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.keys[i], this.values[i]);
		}
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null)
				{
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null && (value == null ? this.values[i] == null : value.equals(this.values[i])))
				{
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]) && (value == null ? this.values[i] == null : value.equals(this.values[i])))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		if (value == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.values[i] == null)
				{
					return true;
				}
			}
			return false;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (value.equals(this.values[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V apply(K key)
	{
		if (key == null)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (this.keys[i] == null)
				{
					return this.values[i];
				}
			}
			return null;
		}
		for (int i = 0; i < this.size; i++)
		{
			if (key.equals(this.keys[i]))
			{
				return this.values[i];
			}
		}
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		int len = this.size + 1;
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		System.arraycopy(this.keys, 0, keys, 0, this.size);
		System.arraycopy(this.values, 0, values, 0, this.size);
		return new ArrayMap(keys, values, len, true);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		int index = this.size;
		int len = index + map.size();
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		System.arraycopy(this.keys, 0, keys, 0, index);
		System.arraycopy(this.values, 0, values, 0, index);
		
		for (Entry<? extends K, ? extends V> entry : map)
		{
			keys[index] = entry.getKey();
			values[index] = entry.getValue();
			index++;
		}
		return new ArrayMap(keys, values, len, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = this.keys[i];
			if (key == null ? k == null : key.equals(k))
			{
				continue;
			}
			
			keys[index] = k;
			values[index] = this.values[i];
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(K key, V value)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = this.keys[i];
			if (key == null ? k == null : key.equals(k))
			{
				continue;
			}
			V v = this.values[i];
			if (value == null ? v == null : value.equals(v))
			{
				continue;
			}
			keys[index] = k;
			values[index] = v;
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(V value)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			V v = this.values[i];
			if (value == null ? v == null : value.equals(v))
			{
				continue;
			}
			
			keys[index] = this.keys[i];
			values[index] = v;
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<? extends K, ? extends V> map)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = this.keys[i];
			if (map.$qmark(k))
			{
				continue;
			}
			
			keys[index] = k;
			values[index] = this.values[i];
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		System.arraycopy(this.keys, 0, keys, 0, this.size);
		for (int i = 0; i < this.size; i++)
		{
			values[i] = mapper.apply(this.keys[i], this.values[i]);
		}
		return new ArrayMap(keys, values, this.size, true);
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = this.keys[i];
			V v = this.values[i];
			if (!condition.test(k, v))
			{
				continue;
			}
			
			keys[index] = k;
			values[index] = this.values[i];
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new ArrayMap(this.keys, this.values, this.size);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		HashMap<K, V> map = new HashMap(this.size);
		for (int i = 0; i < this.size; i++)
		{
			map.update(this.keys[i], this.values[i]);
		}
		return map;
	}
	
	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		builder.append(this.keys[0]).append(" -> ").append(this.values[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ");
			builder.append(this.keys[i]).append(" -> ").append(this.values[i]);
		}
		return builder.append(" ]").toString();
	}
}

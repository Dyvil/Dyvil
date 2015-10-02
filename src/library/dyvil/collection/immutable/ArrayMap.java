package dyvil.collection.immutable;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.tuple.Tuple2;
import dyvil.util.ImmutableException;

@ArrayConvertible
public class ArrayMap<K, V> extends AbstractArrayMap<K, V>implements ImmutableMap<K, V>
{
	public static <K, V> ArrayMap<K, V> apply(Tuple2<K, V>... entries)
	{
		int len = entries.length;
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		int size = AbstractArrayMap.fillEntries(keys, values, entries, len);
		return new ArrayMap<K, V>(keys, values, size, true);
	}
	
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<K, V>();
	}
	
	public static <K, V> Builder<K, V> builder(int capacity)
	{
		return new Builder<K, V>(capacity);
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
	
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private Object[]	keys;
		private Object[]	values;
		private int			size;
		
		public Builder()
		{
			this.keys = new Object[10];
			this.values = new Object[10];
		}
		
		public Builder(int capacity)
		{
			this.keys = new Object[capacity];
			this.values = new Object[capacity];
		}
		
		@Override
		public void put(K key, V value)
		{
			if (this.size < 0)
			{
				throw new IllegalStateException("Already built");
			}
			
			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(this.keys[i], key))
				{
					this.keys[i] = key;
					this.values[i] = value;
					return;
				}
			}
			
			int index = this.size++;
			if (index >= this.keys.length)
			{
				int newCapacity = (int) (this.size * 1.1F);
				Object[] keys = new Object[newCapacity];
				Object[] values = new Object[newCapacity];
				System.arraycopy(this.keys, 0, keys, 0, index);
				System.arraycopy(this.values, 0, values, 0, index);
				this.keys = keys;
				this.values = values;
			}
			this.keys[index] = key;
			this.values[index] = value;
		}
		
		@Override
		public ArrayMap<K, V> build()
		{
			ArrayMap<K, V> map = new ArrayMap<K, V>(this.keys, this.values, this.size, true);
			this.size = -1;
			return map;
		}
	}
	
	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.keys[i]))
			{
				Object[] keys = this.keys.clone();
				Object[] values = this.values.clone();
				values[i] = value;
				return new ArrayMap(keys, values, this.size, true);
			}
		}
		
		int len = this.size + 1;
		Object[] keys = new Object[len];
		Object[] values = new Object[len];
		System.arraycopy(this.keys, 0, keys, 0, this.size);
		System.arraycopy(this.values, 0, values, 0, this.size);
		keys[this.size] = key;
		values[this.size] = value;
		return new ArrayMap(keys, values, len, true);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		int index = this.size;
		int maxLength = index + map.size();
		Object[] keys = new Object[maxLength];
		Object[] values = new Object[maxLength];
		System.arraycopy(this.keys, 0, keys, 0, index);
		System.arraycopy(this.values, 0, values, 0, index);
		
		outer:
		for (Entry<? extends K, ? extends V> entry : map)
		{
			K key = entry.getKey();
			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(keys[i], key))
				{
					values[i] = entry.getValue();
					continue outer;
				}
			}
			keys[index] = entry.getKey();
			values[index++] = entry.getValue();
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (Objects.equals(key, k))
			{
				continue;
			}
			
			keys[index] = k;
			values[index++] = this.values[i];
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
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
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			V v = (V) this.values[i];
			if (Objects.equals(value, v))
			{
				continue;
			}
			
			keys[index] = this.keys[i];
			values[index++] = v;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
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
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> collection)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			K k = (K) this.keys[i];
			if (collection.contains(k))
			{
				continue;
			}
			
			keys[index] = k;
			values[index++] = this.values[i];
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
			values[i] = mapper.apply((K) this.keys[i], (V) this.values[i]);
		}
		return new ArrayMap(keys, values, this.size, true);
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
		int newSize = 0;
		outer:
		for (int i = 0; i < this.size; i++)
		{
			Entry<? extends U, ? extends R> entry = mapper.apply((K) this.keys[i], (V) this.values[i]);
			if (entry == null)
			{
				continue;
			}
			
			U key = entry.getKey();
			R value = entry.getValue();
			
			for (int j = 0; j < i; j++)
			{
				if (Objects.equals(keys[j], key))
				{
					values[j] = value;
					continue outer;
				}
			}
			
			keys[newSize] = key;
			values[newSize++] = value;
		}
		return null;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		Builder<U, R> builder = new Builder();
		
		for (int i = 0; i < this.size; i++)
		{
			for (Entry<? extends U, ? extends R> entry : mapper.apply((K) this.keys[i], (V) this.values[i]))
			{
				builder.put(entry);
			}
		}
		
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		
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
			values[index] = this.values[i];
			index++;
		}
		return new ArrayMap(keys, values, index, true);
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		Object[] keys = new Object[this.size];
		Object[] values = new Object[this.size];
		System.arraycopy(this.keys, 0, values, 0, this.size);
		System.arraycopy(this.values, 0, keys, 0, this.size);
		return new ArrayMap(keys, values, this.size, true);
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new ArrayMap(this.keys, this.values, this.size);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.ArrayMap(this.keys, this.values, this.size);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

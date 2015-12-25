package dyvil.collection.immutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractArrayMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.tuple.Tuple2;
import dyvil.annotation.Immutable;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@ArrayConvertible
@Immutable
public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = 4583062458335627011L;
	
	public static <K, V> ArrayMap<K, V> apply(Tuple2<K, V>... tuples)
	{
		return new ArrayMap(tuples);
	}
	
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<K, V>();
	}
	
	public static <K, V> Builder<K, V> builder(int capacity)
	{
		return new Builder<K, V>(capacity);
	}
	
	protected ArrayMap(int capacity)
	{
		super(capacity);
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
	
	public ArrayMap(AbstractArrayMap<K, V> map)
	{
		super(map);
	}
	
	public ArrayMap(Tuple2<K, V>... tuples)
	{
		super(tuples);
	}
	
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private ArrayMap<K, V> map;
		
		public Builder()
		{
			this.map = new ArrayMap(DEFAULT_CAPACITY);
		}
		
		public Builder(int capacity)
		{
			this.map = new ArrayMap(capacity);
		}
		
		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built");
			}
			
			this.map.putInternal(key, value);
		}
		
		@Override
		public ArrayMap<K, V> build()
		{
			ArrayMap<K, V> map = this.map;
			this.map = null;
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
		ArrayMap<K, V> copy = new ArrayMap<K, V>(this);
		copy.putInternal(key, value);
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		ArrayMap<K, V> copy = new ArrayMap<K, V>(this.size + map.size());
		
		// Copy our keys and values
		System.arraycopy(this.keys, 0, copy.keys, 0, this.size);
		System.arraycopy(this.values, 0, copy.values, 0, this.size);
		
		// Put the new keys and values
		for (Entry<? extends K, ? extends V> entry : map)
		{
			copy.putInternal(entry.getKey(), entry.getValue());
		}
		return copy;
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
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		ArrayMap<NK, V> copy = new ArrayMap<NK, V>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			V value = (V) this.values[i];
			copy.putInternal(mapper.apply((K) this.keys[i], value), value);
		}
		return copy;
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
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
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		ArrayMap<NK, NV> copy = new ArrayMap<NK, NV>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			Entry<? extends NK, ? extends NV> entry = mapper.apply((K) this.keys[i], (V) this.values[i]);
			if (entry == null)
			{
				continue;
			}
			
			copy.putInternal(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		ArrayMap<NK, NV> copy = new ArrayMap<NK, NV>(this.size);
		for (int i = 0; i < this.size; i++)
		{
			for (Entry<? extends NK, ? extends NV> entry : mapper.apply((K) this.keys[i], (V) this.values[i]))
			{
				copy.putInternal(entry.getKey(), entry.getValue());
			}
		}
		return copy;
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
		return new ArrayMap(this);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.ArrayMap(this);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

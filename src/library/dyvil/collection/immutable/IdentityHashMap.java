package dyvil.collection.immutable;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractIdentityHashMap;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.NilConvertible;
import dyvil.tuple.Tuple2;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@NilConvertible
@ArrayConvertible
public class IdentityHashMap<K, V> extends AbstractIdentityHashMap<K, V>implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = 7106880090218416170L;
	
	public static <K, V> IdentityHashMap<K, V> apply()
	{
		return new IdentityHashMap();
	}
	
	public static <K, V> IdentityHashMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new IdentityHashMap(entries);
	}
	
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<K, V>();
	}
	
	public static <K, V> Builder<K, V> builder(int capacity)
	{
		return new Builder<K, V>(capacity);
	}
	
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private IdentityHashMap<K, V> map;
		
		public Builder()
		{
			this.map = new IdentityHashMap<K, V>();
		}
		
		public Builder(int capacity)
		{
			this.map = new IdentityHashMap<K, V>(capacity);
		}
		
		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built!");
			}
			
			this.map.putInternal(key, value);
		}
		
		@Override
		public IdentityHashMap<K, V> build()
		{
			IdentityHashMap<K, V> map = this.map;
			this.map = null;
			return map;
		}
	}
	
	protected IdentityHashMap()
	{
		super(AbstractIdentityHashMap.DEFAULT_CAPACITY);
	}
	
	protected IdentityHashMap(int capacity)
	{
		super(capacity);
	}
	
	public IdentityHashMap(Map<K, V> map)
	{
		super(map);
	}
	
	public IdentityHashMap(AbstractIdentityHashMap<K, V> map)
	{
		super(map);
	}
	
	public IdentityHashMap(Tuple2<K, V>... entries)
	{
		super(entries);
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		IdentityHashMap<K, V> map = new IdentityHashMap(this);
		map.ensureCapacity(this.size + 1);
		map.putInternal(key, value);
		return map;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this);
		copy.ensureCapacity(this.size + map.size());
		copy.putInternal(map);
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			K k = entry.getKey();
			if (k != key)
			{
				copy.putInternal(k, entry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			K k = entry.getKey();
			V v = entry.getValue();
			if (k != key && v != value)
			{
				copy.putInternal(k, v);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			V v = entry.getValue();
			if (v != value)
			{
				copy.putInternal(entry.getKey(), v);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			K k = entry.getKey();
			V v = entry.getValue();
			if (!map.contains(k, v))
			{
				copy.putInternal(k, v);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			K k = entry.getKey();
			if (!keys.contains(k))
			{
				copy.putInternal(k, entry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		IdentityHashMap<NK, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			V value = entry.getValue();
			copy.putInternal(mapper.apply(entry.getKey(), value), value);
		}
		return copy;
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		IdentityHashMap<K, NV> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			K key = entry.getKey();
			copy.putInternal(key, mapper.apply(key, entry.getValue()));
		}
		return copy;
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		IdentityHashMap<NK, NV> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			Entry<? extends NK, ? extends NV> result = mapper.apply(entry.getKey(), entry.getValue());
			if (result != null)
			{
				copy.putInternal(result.getKey(), result.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		IdentityHashMap<NK, NV> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			for (Entry<? extends NK, ? extends NV> result : mapper.apply(entry.getKey(), entry.getValue()))
			{
				copy.putInternal(result.getKey(), result.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		IdentityHashMap<K, V> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			K key = entry.getKey();
			V value = entry.getValue();
			if (condition.test(key, value))
			{
				copy.putInternal(key, value);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		IdentityHashMap<V, K> copy = new IdentityHashMap(this.size);
		for (Entry<K, V> entry : this)
		{
			copy.putInternal(entry.getValue(), entry.getKey());
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new IdentityHashMap<K, V>(this);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.IdentityHashMap<K, V>(this);
	}
}

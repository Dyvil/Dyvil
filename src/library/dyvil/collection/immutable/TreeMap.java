package dyvil.collection.immutable;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractTreeMap;
import dyvil.tuple.Tuple2;

@ArrayConvertible
public class TreeMap<K, V> extends AbstractTreeMap<K, V>implements ImmutableMap<K, V>
{
	public static <K extends Comparable<K>, V> TreeMap<K, V> apply(Tuple2<K, V>... entries)
	{
		TreeMap<K, V> map = new TreeMap();
		for (Tuple2<K, V> entry : entries)
		{
			map.putUnsafe(entry._1, entry._2);
		}
		return map;
	}
	
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder<K, V>();
	}
	
	public TreeMap()
	{
	}
	
	public TreeMap(Map<? extends K, ? extends V> map)
	{
		super(map, null);
	}
	
	public TreeMap(Map<? extends K, ? extends V> m, Comparator<? super K> comparator)
	{
		super(m, comparator);
	}
	
	protected static final class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private TreeMap<K, V> map = new TreeMap<K, V>();
		
		@Override
		public void put(K key, V value)
		{
			if (this.map == null)
			{
				throw new IllegalStateException("Already built!");
			}
			this.map.putUnsafe(key, value);
		}
		
		@Override
		public ImmutableMap<K, V> build()
		{
			TreeMap<K, V> map = this.map;
			this.map = null;
			return map;
		}
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		copy.putUnsafe(key, value);
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		for (Entry<? extends K, ? extends V> entry : map)
		{
			copy.putUnsafe(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		boolean found = false;
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			if (!found && Objects.equals(key, entryKey))
			{
				found = true;
				continue;
			}
			
			copy.putUnsafe(entryKey, entry.getValue());
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		boolean found = false;
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!found && Objects.equals(key, entryKey))
			{
				found = true;
				if (Objects.equals(value, entryValue))
				{
					continue;
				}
			}
			
			copy.putUnsafe(entryKey, entryValue);
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			V entryValue = entry.getValue();
			if (!Objects.equals(value, entryValue))
			{
				copy.putUnsafe(entry.getKey(), entryValue);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			V entryValue = entry.getValue();
			if (!map.contains(entryKey, entryValue))
			{
				copy.putUnsafe(entryKey, entryValue);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K entryKey = entry.getKey();
			if (!keys.contains(entryKey))
			{
				copy.putUnsafe(entryKey, entry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		TreeMap<K, U> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K key = entry.getKey();
			V value = entry.getValue();
			copy.putUnsafe(key, mapper.apply(key, value));
		}
		return null;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		TreeMap<U, R> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K key = entry.getKey();
			V value = entry.getValue();
			Entry<? extends U, ? extends R> newEntry = mapper.apply(key, value);
			if (newEntry != null)
			{
				copy.putUnsafe(newEntry.getKey(), newEntry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		TreeMap<U, R> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K key = entry.getKey();
			V value = entry.getValue();
			for (Entry<? extends U, ? extends R> newEntry : mapper.apply(key, value))
			{
				copy.putUnsafe(newEntry.getKey(), newEntry.getValue());
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		TreeMap<K, V> copy = new TreeMap(this, this.comparator);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			K key = entry.getKey();
			V value = entry.getValue();
			if (condition.test(key, value))
			{
				copy.putUnsafe(key, value);
			}
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		TreeMap<V, K> copy = new TreeMap(this);
		for (TreeEntry<K, V> entry = this.getFirstEntry(); entry != null; entry = successor(entry))
		{
			copy.putUnsafe(entry.getValue(), entry.getKey());
		}
		return copy;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new TreeMap(this, this.comparator);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.TreeMap(this, this.comparator);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

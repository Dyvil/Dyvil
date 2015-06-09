package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.iterator.ArrayIterator;
import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.tuple.Tuple2;

@ArrayConvertible
public class TupleMap<K, V> implements ImmutableMap<K, V>
{
	private final int				size;
	private final Tuple2<K, V>[]	entries;
	
	public static <K, V> TupleMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new TupleMap(entries);
	}
	
	public TupleMap(Tuple2<K, V>[] entries)
	{
		this.size = entries.length;
		this.entries = new Tuple2[this.size];
		System.arraycopy(entries, 0, this.entries, 0, this.size);
	}
	
	public TupleMap(Tuple2<K, V>[] entries, int size)
	{
		this.size = size;
		this.entries = new Tuple2[size];
		System.arraycopy(entries, 0, this.entries, 0, size);
	}
	
	public TupleMap(Tuple2<K, V>[] entries, boolean trusted)
	{
		this.size = entries.length;
		this.entries = entries;
	}
	
	public TupleMap(Tuple2<K, V>[] entries, int size, boolean trusted)
	{
		this.size = size;
		this.entries = entries;
	}
	
	public TupleMap(Map<K, V> map)
	{
		this.size = map.size();
		this.entries = new Tuple2[this.size];
		
		int index = 0;
		for (Entry<K, V> entry : map)
		{
			this.entries[index++] = new Tuple2(entry.getKey(), entry.getValue());
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
		return new ArrayIterator<>(this.entries, this.size);
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
				return this.index < TupleMap.this.size;
			}
			
			@Override
			public K next()
			{
				return TupleMap.this.entries[this.index++]._1;
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
				return this.index < TupleMap.this.size;
			}
			
			@Override
			public V next()
			{
				return TupleMap.this.entries[this.index++]._2;
			}
		};
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]);
		}
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			action.accept(entry._1, entry._2);
		}
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				if (value == entry._2 || value != null && value.equals(entry._2))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (value == entry._2 || value != null && value.equals(entry._2))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V apply(K key)
	{
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (key == entry._1 || key != null && key.equals(entry._1))
			{
				return entry._2;
			}
		}
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<? super K, ? super V> map)
	{
		return null;
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return null;
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return null;
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		builder.append(this.entries[0]);
		for (int i = 1; i < this.size; i++)
		{
			builder.append(", ");
			builder.append(this.entries[i]);
		}
		return builder.append(" ]").toString();
	}
}

package dyvil.collection.immutable;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.*;
import dyvil.collection.iterator.ArrayIterator;
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
			this.entries[index++] = entry.toTuple();
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
	public void forEachKey(Consumer<? super K> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]._1);
		}
	}
	
	@Override
	public void forEachValue(Consumer<? super V> action)
	{
		for (int i = 0; i < this.size; i++)
		{
			action.accept(this.entries[i]._2);
		}
	}
	
	@Override
	public boolean containsKey(Object key)
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
	public boolean contains(Object key, Object value)
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
	public boolean containsValue(Object value)
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
	public V get(Object key)
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
		for (int i = 0; i < this.size; i++)
		{
			if (Objects.equals(key, this.entries[i]._1))
			{
				Tuple2[] entries = this.entries.clone();
				entries[i] = new Tuple2(key, value);
				return new TupleMap(entries, this.size, true);
			}
		}
		
		Tuple2[] entries = new Tuple2[this.size + 1];
		System.arraycopy(this.entries, 0, entries, 0, this.size);
		entries[this.size] = new Tuple2(key, value);
		return new TupleMap(entries, this.size + 1, true);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		int index = this.size;
		int maxLength = index + map.size();
		Tuple2[] entries = new Tuple2[maxLength];
		System.arraycopy(this.entries, 0, entries, 0, index);
		
		outer:
		for (Entry<? extends K, ? extends V> entry : map)
		{
			K key = entry.getKey();
			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(entries[i]._1, key))
				{
					entries[i] = new Tuple2(key, entry.getValue());
					continue outer;
				}
			}
			entries[index++] = entry.toTuple();
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(key, entry._1) && Objects.equals(value, entry._2))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (Objects.equals(value, entry._2))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (map.contains(entry))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (keys.contains(entry._1))
			{
				continue;
			}
			
			entries[index++] = entry;
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			K key = entry._1;
			entries[i] = new Tuple2(key, mapper.apply(key, entry._2));
		}
		return new TupleMap(entries, this.size, true);
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		outer:
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			Entry<? extends U, ? extends R> newEntry = mapper.apply(entry._1, entry._2);
			if (newEntry == null)
			{
				continue;
			}
			
			Tuple2<? extends U, ? extends R> newTuple = newEntry.toTuple();
			U key = newTuple._1;
			for (int j = 0; j < index; j++)
			{
				if (Objects.equals(entries[j]._1, key))
				{
					entries[j] = newTuple;
					continue outer;
				}
			}
			
			entries[index++] = newTuple;
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		Tuple2[] entries = new Tuple2[this.size << 2];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			
			outer:
			for (Entry<? extends U, ? extends R> newEntry : mapper.apply(entry._1, entry._2))
			{
				Tuple2<? extends U, ? extends R> newTuple = newEntry.toTuple();
				U key = newTuple._1;
				for (int j = 0; j < index; j++)
				{
					if (Objects.equals(entries[j]._1, key))
					{
						entries[j] = newTuple;
						continue outer;
					}
				}
				
				int index1 = index++;
				if (index1 >= entries.length)
				{
					Tuple2[] temp = new Tuple2[index << 1];
					System.arraycopy(entries, 0, temp, 0, index1);
					entries = temp;
				}
				entries[index1] = newTuple;
			}
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Tuple2[] entries = new Tuple2[this.size];
		
		int index = 0;
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			if (condition.test(entry._1, entry._2))
			{
				entries[index++] = entry;
			}
		}
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		Tuple2<V, K>[] entries = new Tuple2[this.size];
		int index = 0;
		outer:
		for (int i = 0; i < this.size; i++)
		{
			Tuple2<K, V> entry = this.entries[i];
			V value = entry._2;
			Tuple2<V, K> newEntry = new Tuple2<V, K>(value, entry._1);
			
			for (int j = 0; j < index; j++)
			{
				if (Objects.equals(entries[j]._1, value))
				{
					entries[j] = newEntry;
					continue outer;
				}
			}
			
			entries[index++] = newEntry;
		}
		
		return new TupleMap(entries, index, true);
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new TupleMap(this.entries);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.ArrayMap(this);
	}
	
	@Override
	public String toString()
	{
		if (this.size <= 0)
		{
			return "[]";
		}
		
		StringBuilder builder = new StringBuilder("[ ");
		Tuple2<K, V> entry = this.entries[0];
		builder.append(entry._1).append(" -> ").append(entry._2);
		for (int i = 1; i < this.size; i++)
		{
			entry = this.entries[i];
			builder.append(", ").append(entry._1).append(" -> ").append(entry._2);
		}
		return builder.append(" ]").toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return Map.mapEquals(this, obj);
	}
	
	@Override
	public int hashCode()
	{
		return Map.mapHashCode(this);
	}
}

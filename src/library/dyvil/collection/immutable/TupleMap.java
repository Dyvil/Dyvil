package dyvil.collection.immutable;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.literal.ArrayConvertible;

import dyvil.collection.*;
import dyvil.collection.impl.AbstractTupleMap;
import dyvil.tuple.Tuple2;
import dyvil.util.ImmutableException;

@ArrayConvertible
public class TupleMap<K, V> extends AbstractTupleMap<K, V>implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = -5372836862143742212L;
	
	public static <K, V> TupleMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new TupleMap(entries);
	}
	
	public static <K, V> TupleMap<K, V> fromArray(Tuple2<K, V>[] entries)
	{
		return new TupleMap(entries, true);
	}
	
	public static <K, V> Builder<K, V> builder()
	{
		return new Builder();
	}
	
	public TupleMap(Tuple2<K, V>... entries)
	{
		super(entries);
	}
	
	public TupleMap(Tuple2<K, V>[] entries, int size)
	{
		super(entries, size);
	}
	
	public TupleMap(Tuple2<K, V>[] entries, boolean trusted)
	{
		super(entries, trusted);
	}
	
	public TupleMap(Tuple2<K, V>[] entries, int size, boolean trusted)
	{
		super(entries, size, trusted);
	}
	
	public TupleMap(Map<K, V> map)
	{
		super(map);
	}
	
	public static class Builder<K, V> implements ImmutableMap.Builder<K, V>
	{
		private Tuple2<K, V>[]	entries;
		private int				size;
		
		public Builder()
		{
			this.entries = new Tuple2[DEFAULT_CAPACITY];
		}
		
		public Builder(int capacity)
		{
			this.entries = new Tuple2[capacity];
		}
		
		private void put(K key, Tuple2<K, V> newEntry)
		{
			for (int i = 0; i < this.size; i++)
			{
				if (Objects.equals(this.entries[i]._1, key))
				{
					this.entries[i] = newEntry;
					return;
				}
			}
			
			int index = this.size++;
			if (index >= this.entries.length)
			{
				Tuple2<K, V>[] temp = new Tuple2[(int) (this.size * 1.1F)];
				System.arraycopy(this.entries, 0, temp, 0, index);
				this.entries = temp;
			}
			this.entries[index] = newEntry;
		}
		
		@Override
		public void put(K key, V value)
		{
			if (this.size < 0)
			{
				throw new IllegalStateException("Already built");
			}
			
			this.put(key, new Tuple2<K, V>(key, value));
		}
		
		@Override
		public void put(Entry<? extends K, ? extends V> entry)
		{
			if (this.size < 0)
			{
				throw new IllegalStateException("Already built");
			}
			
			this.put(entry.getKey(), (Tuple2) entry.toTuple());
		}
		
		@Override
		public TupleMap<K, V> build()
		{
			TupleMap<K, V> map = new TupleMap(this.entries, this.size, true);
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
		return new dyvil.collection.mutable.TupleMap(this.entries, this.size);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

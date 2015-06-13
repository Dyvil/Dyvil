package dyvil.collection.mutable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import sun.misc.SharedSecrets;
import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.lang.Entry;
import dyvil.lang.Map;
import dyvil.lang.Type;
import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.TypeConvertible;

@ClassConvertible
@TypeConvertible
public class EnumMap<K extends Enum<K>, V> implements MutableMap<K, V>
{
	private class EnumEntry implements Entry<K, V>
	{
		int	index;
		
		EnumEntry(int index)
		{
			this.index = index;
		}
		
		@Override
		public K getKey()
		{
			return keys[this.index];
		}
		
		@Override
		public V getValue()
		{
			return (V) values[this.index];
		}
		
		@Override
		public String toString()
		{
			return keys[this.index] + " -> " + values[this.index];
		}
		
		@Override
		public boolean equals(Object obj)
		{
			return Entry.entryEquals(this, obj);
		}
		
		@Override
		public int hashCode()
		{
			return Entry.entryHashCode(this);
		}
	}
	
	private abstract class EnumIterator<E> implements Iterator<E>
	{
		private int		index;
		private boolean	indexValid;
		
		@Override
		public boolean hasNext()
		{
			Object[] tab = values;
			for (int i = this.index; i < tab.length; i++)
			{
				Object key = tab[i];
				if (key != null)
				{
					this.index = i;
					return this.indexValid = true;
				}
			}
			this.index = tab.length;
			return false;
		}
		
		protected int nextIndex()
		{
			if (!this.indexValid && !this.hasNext())
			{
				throw new NoSuchElementException();
			}
			
			this.indexValid = false;
			return this.index++;
		}
	}
	
	protected Class<K>	type;
	protected K[]		keys;
	protected Object[]	values;
	protected int		size;
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Type<K> type)
	{
		return new EnumMap(type);
	}
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Class<K> type)
	{
		return new EnumMap(type);
	}
	
	EnumMap(Class<K> type, K[] keys, V[] values, int size)
	{
		this.type = type;
		this.keys = keys;
		this.values = values.clone();
		this.size = size;
	}
	
	public EnumMap(Type<K> type)
	{
		this(type.getTheClass());
	}
	
	public EnumMap(Class<K> type)
	{
		this.keys = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(type);
		this.values = new Object[this.keys.length];
		this.type = type;
	}
	
	private boolean checkType(Object key)
	{
		return key != null && key.getClass() == this.type;
	}
	
	private static int index(Object key)
	{
		return ((Enum) key).ordinal();
	}
	
	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return new EnumIterator<Entry<K, V>>()
		{
			@Override
			public Entry<K, V> next()
			{
				return new EnumEntry(this.nextIndex());
			}
			
			@Override
			public String toString()
			{
				return "EntryIterator(" + EnumMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return new EnumIterator<K>()
		{
			@Override
			public K next()
			{
				return EnumMap.this.keys[this.nextIndex()];
			}
			
			@Override
			public String toString()
			{
				return "KeyIterator(" + EnumMap.this + ")";
			}
		};
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return new EnumIterator<V>()
		{
			@Override
			public V next()
			{
				return (V) EnumMap.this.values[this.nextIndex()];
			}
			
			@Override
			public String toString()
			{
				return "ValueIterator(" + EnumMap.this + ")";
			}
		};
	}
	
	@Override
	public boolean $qmark(Object key)
	{
		if (!this.checkType(key))
		{
			return false;
		}
		
		return this.values[index(key)] != null;
	}
	
	@Override
	public boolean $qmark(Object key, Object value)
	{
		if (!this.checkType(key))
		{
			return false;
		}
		
		Object v = this.values[((Enum) key).ordinal()];
		return Objects.equals(v, value);
	}
	
	@Override
	public boolean $qmark$colon(V value)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			if (Objects.equals(this.values[i], value))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public V apply(K key)
	{
		if (!this.checkType(key))
		{
			return null;
		}
		
		return (V) this.values[index(key)];
	}
	
	@Override
	public MutableMap<K, V> $plus(K key, V value)
	{
		if (!this.checkType(key))
		{
			return this;
		}
		
		MutableMap<K, V> copy = this.copy();
		copy.update(key, value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$plus$plus$eq(map);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(Object key)
	{
		if (!this.checkType(key))
		{
			return this;
		}
		
		MutableMap<K, V> copy = this.copy();
		copy.$minus$eq(key);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus(Object key, Object value)
	{
		if (!this.checkType(key))
		{
			return this;
		}
		
		MutableMap<K, V> copy = this.copy();
		copy.remove(key, value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$colon(Object value)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$colon$eq(value);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> $minus$minus(Map<? super K, ? super V> map)
	{
		MutableMap<K, V> copy = this.copy();
		copy.$minus$minus$eq(map);
		return copy;
	}
	
	@Override
	public <U> MutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		MutableMap<K, U> copy = (MutableMap<K, U>) this.copy();
		copy.map((BiFunction<? super K, ? super U, ? extends U>) mapper);
		return copy;
	}
	
	@Override
	public MutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		MutableMap<K, V> copy = this.copy();
		copy.filter(condition);
		return copy;
	}
	
	@Override
	public void clear()
	{
		this.size = 0;
		Arrays.fill(this.values, null);
	}
	
	@Override
	public V put(K key, V value)
	{
		if (!this.checkType(key))
		{
			return null;
		}
		
		int index = index(key);
		V oldValue = (V) this.values[index];
		this.values[index] = value;
		if (oldValue == null)
		{
			this.size++;
		}
		return oldValue;
	}
	
	@Override
	public V remove(Object key)
	{
		if (!this.checkType(key))
		{
			return null;
		}
		
		int index = index(key);
		V oldValue = (V) this.values[index];
		
		if (oldValue != null)
		{
			this.size--;
			this.values[index] = null;
			return oldValue;
		}
		return null;
	}
	
	@Override
	public boolean remove(Object key, Object value)
	{
		if (!this.checkType(key))
		{
			return false;
		}
		
		int index = index(key);
		Object oldValue = this.values[index];
		if (oldValue != null && oldValue.equals(value))
		{
			this.size--;
			return true;
		}
		return false;
	}
	
	@Override
	public void $minus$colon$eq(Object value)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			if (Objects.equals(this.values[i], value))
			{
				this.size--;
				this.values[i] = null;
			}
		}
	}
	
	@Override
	public void map(BiFunction<? super K, ? super V, ? extends V> mapper)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			V v = (V) this.values[i];
			if (v != null)
			{
				this.values[i] = mapper.apply(this.keys[i], v);
			}
		}
	}
	
	@Override
	public void filter(BiPredicate<? super K, ? super V> condition)
	{
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			V v = (V) this.values[i];
			if (v != null && !condition.test(this.keys[i], v))
			{
				this.values[i] = null;
				this.size--;
			}
		}
	}
	
	@Override
	public MutableMap<K, V> copy()
	{
		return new EnumMap(this.type, this.keys, this.values, this.size);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return Map.mapToString(this);
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

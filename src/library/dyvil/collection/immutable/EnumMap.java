package dyvil.collection.immutable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import dyvil.lang.Type;
import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.TypeConvertible;

import dyvil.annotation.sealed;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractEnumMap;
import dyvil.util.ImmutableException;

@ClassConvertible
@TypeConvertible
public class EnumMap<K extends Enum<K>, V> extends AbstractEnumMap<K, V>implements ImmutableMap<K, V>
{
	public static <K extends Enum<K>, V> Builder<K, V> builder(Type<K> type)
	{
		return new Builder(type.getTheClass());
	}
	
	public static <K extends Enum<K>, V> Builder<K, V> builder(Class<K> type)
	{
		return new Builder(type);
	}
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Type<K> type)
	{
		return new EnumMap(type);
	}
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Class<K> type)
	{
		return new EnumMap(type);
	}
	
	public @sealed EnumMap(Class<K> type, K[] keys, V[] values, int size)
	{
		super(type, keys, values, size);
	}
	
	public EnumMap(Class<K> type)
	{
		super(type);
	}
	
	public EnumMap(Type<K> type)
	{
		super(type.getTheClass());
	}
	
	public static class Builder<K extends Enum<K>, V> implements ImmutableMap.Builder<K, V>
	{
		private Class<K>	type;
		private Object[]	values;
		private int			size;
		
		public Builder(Class<K> type)
		{
			this.type = type;
			this.values = new Object[getKeys(type).length];
		}
		
		@Override
		public void put(K key, V value)
		{
			if (this.size < 0)
			{
				throw new IllegalStateException("Already built");
			}
			if (!checkType(this.type, key))
			{
				return;
			}
			
			int index = index(key);
			if (this.values[index] == null)
			{
				this.size++;
			}
			this.values[index] = value;
		}
		
		@Override
		public EnumMap<K, V> build()
		{
			EnumMap<K, V> map = new EnumMap(this.type, getKeys(this.type), this.values, this.size);
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
		if (!checkType(this.type, key))
		{
			return this;
		}
		
		int index = index(key);
		Object[] newValues = this.values.clone();
		int newSize = this.size;
		if (newValues[index] == null)
		{
			newSize++;
		}
		newValues[index] = value;
		return new EnumMap(this.type, this.keys, newValues, newSize);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		Object[] newValues = this.values.clone();
		int newSize = this.size;
		
		for (Entry<? extends K, ? extends V> entry : map)
		{
			K key = entry.getKey();
			V value = entry.getValue();
			int index = index(key);
			if (newValues[index] == null)
			{
				newSize++;
			}
			newValues[index] = value;
		}
		return new EnumMap(this.type, this.keys, newValues, newSize);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		if (!checkType(this.type, key))
		{
			return this;
		}
		
		int index = index(key);
		if (this.values[index] == null)
		{
			return this;
		}
		
		Object[] newValues = this.values.clone();
		newValues[index] = null;
		return new EnumMap(this.type, this.keys, newValues, this.size - 1);
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		if (!checkType(this.type, key))
		{
			return this;
		}
		
		int index = index(key);
		if (!Objects.equals(this.values[index], value))
		{
			return this;
		}
		
		Object[] newValues = this.values.clone();
		newValues[index] = null;
		return new EnumMap(this.type, this.keys, newValues, this.size - 1);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		Object[] newValues = this.values.clone();
		int newSize = this.size;
		
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			if (Objects.equals(value, this.values[i]))
			{
				newValues[i] = null;
				newSize--;
			}
		}
		return new EnumMap(this.type, this.keys, newValues, newSize);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		Object[] newValues = this.values.clone();
		int newSize = this.size;
		
		for (Entry<?, ?> entry : map)
		{
			Object key = entry.getKey();
			int index = index(key);
			V value = (V) newValues[index];
			if (value != null && Objects.equals(value, entry.getValue()))
			{
				newSize--;
				newValues[index] = null;
			}
		}
		return new EnumMap(this.type, this.keys, newValues, newSize);
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		Object[] newValues = this.values.clone();
		int newSize = this.size;
		
		for (Object key : keys)
		{
			int index = index(key);
			V value = (V) newValues[index];
			if (value != null)
			{
				newSize--;
				newValues[index] = null;
			}
		}
		return new EnumMap(this.type, this.keys, newValues, newSize);
	}
	
	@Override
	public <U> ImmutableMap<K, U> mapped(BiFunction<? super K, ? super V, ? extends U> mapper)
	{
		int len = this.values.length;
		Object[] newValues = new Object[len];
		
		for (int i = 0; i < len; i++)
		{
			Object value = this.values[i];
			if (value != null)
			{
				newValues[i] = mapper.apply(this.keys[i], (V) value);
			}
		}
		return new EnumMap(this.type, this.keys, newValues, this.size);
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		ImmutableMap.Builder<U, R> builder = new ArrayMap.Builder(this.size);
		
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			Object value = this.values[i];
			if (value == null)
			{
				continue;
			}
			
			Entry<? extends U, ? extends R> entry = mapper.apply(this.keys[i], (V) value);
			if (entry != null)
			{
				builder.put(entry);
			}
		}
		
		return builder.build();
	}
	
	@Override
	public <U, R> ImmutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		ImmutableMap.Builder<U, R> builder = new ArrayMap.Builder(this.size);
		
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			Object value = this.values[i];
			if (value == null)
			{
				continue;
			}
			
			for (Entry<? extends U, ? extends R> entry : mapper.apply(this.keys[i], (V) value))
			{
				builder.put(entry);
			}
		}
		
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		Object[] newValues = this.values.clone();
		int newSize = this.size;
		int len = this.values.length;
		
		for (int i = 0; i < len; i++)
		{
			Object value = this.values[i];
			if (value != null)
			{
				if (!condition.test(this.keys[i], (V) value))
				{
					newValues[i] = null;
					newSize--;
				}
			}
		}
		return new EnumMap(this.type, this.keys, newValues, newSize);
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		ImmutableMap.Builder<V, K> builder = new ArrayMap.Builder(this.size);
		
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			builder.put((V) this.values[i], this.keys[i]);
		}
		return builder.build();
	}
	
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new EnumMap(this.type, this.keys, this.values.clone(), this.size);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.EnumMap(this.type, this.keys, this.values.clone(), this.size);
	}
}

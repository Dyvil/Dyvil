package dyvil.collection.immutable;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractEnumMap;
import dyvil.lang.Type;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.reflect.EnumReflection;
import dyvil.reflect.Modifiers;
import dyvil.tuple.Tuple2;
import dyvil.annotation.Immutable;
import dyvil.util.ImmutableException;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@ArrayConvertible
@Immutable
public class EnumMap<K extends Enum<K>, V> extends AbstractEnumMap<K, V> implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = -2305035920228304893L;
	
	@SafeVarargs
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new EnumMap<>(entries);
	}
	
	public static <K extends Enum<K>, V> Builder<K, V> builder(Type<K> type)
	{
		return new Builder<>(type.getTheClass());
	}
	
	public static <K extends Enum<K>, V> Builder<K, V> builder(Class<K> type)
	{
		return new Builder<>(type);
	}

	@DyvilModifiers(Modifiers.INTERNAL)
	private
	EnumMap(Class<K> type, K[] keys, V[] values, int size)
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
	
	public EnumMap(Map<K, V> map)
	{
		super(map);
	}
	
	public EnumMap(AbstractEnumMap<K, V> map)
	{
		super(map);
	}
	
	@SafeVarargs
	public EnumMap(Tuple2<K, V>... tuples)
	{
		super(tuples);
	}
	
	public static class Builder<K extends Enum<K>, V> implements ImmutableMap.Builder<K, V>
	{
		private Class<K> type;
		private Object[] values;
		private int      size;
		
		public Builder(Class<K> type)
		{
			this.type = type;
			this.values = new Object[EnumReflection.getEnumCount(type)];
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
			EnumMap<K, V> map = new EnumMap(this.type, EnumReflection.getEnumConstants(this.type), this.values,
			                                this.size);
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
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		int len = this.values.length;
		ImmutableMap.Builder<NK, V> builder = new ArrayMap.Builder(this.size);
		
		for (int i = 0; i < len; i++)
		{
			V value = (V) this.values[i];
			if (value != null)
			{
				builder.put(mapper.apply(this.keys[i], value), value);
			}
		}
		return builder.build();
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
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
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		ImmutableMap.Builder<NK, NV> builder = new ArrayMap.Builder(this.size);
		
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			Object value = this.values[i];
			if (value == null)
			{
				continue;
			}
			
			Entry<? extends NK, ? extends NV> entry = mapper.apply(this.keys[i], (V) value);
			if (entry != null)
			{
				builder.put(entry);
			}
		}
		
		return builder.build();
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		ImmutableMap.Builder<NK, NV> builder = new ArrayMap.Builder(this.size);
		
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			Object value = this.values[i];
			if (value == null)
			{
				continue;
			}
			
			for (Entry<? extends NK, ? extends NV> entry : mapper.apply(this.keys[i], (V) value))
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
		return new EnumMap<>(this);
	}
	
	@Override
	public MutableMap<K, V> mutable()
	{
		return new dyvil.collection.mutable.EnumMap<>(this);
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

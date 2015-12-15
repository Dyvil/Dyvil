package dyvil.collection.mutable;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.Map;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractEnumMap;
import dyvil.lang.Type;
import dyvil.lang.literal.ArrayConvertible;
import dyvil.lang.literal.ClassConvertible;
import dyvil.lang.literal.TypeConvertible;
import dyvil.reflect.Modifiers;
import dyvil.tuple.Tuple2;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@ClassConvertible
@TypeConvertible
@ArrayConvertible
public class EnumMap<K extends Enum<K>, V> extends AbstractEnumMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = 1734016065128722262L;
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Type<K> type)
	{
		return new EnumMap<>(type.getTheClass());
	}
	
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Class<K> type)
	{
		return new EnumMap<>(type);
	}
	
	@SafeVarargs
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(Tuple2<K, V>... entries)
	{
		return new EnumMap<>(entries);
	}

	@DyvilModifiers(Modifiers.INTERNAL)
	public EnumMap(Class<K> type, K[] keys, V[] values, int size)
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
	
	@Override
	protected void removeAt(int index)
	{
		this.values[index] = null;
		this.size--;
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
		if (!checkType(this.type, key))
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
	public boolean putIfAbsent(K key, V value)
	{
		if (!checkType(this.type, key))
		{
			return false;
		}
		
		int index = index(key);
		if (this.values[index] == null)
		{
			this.values[index] = value;
			this.size++;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		if (!checkType(this.type, key))
		{
			return false;
		}
		
		int index = index(key);
		V value = (V) this.values[index];
		if (value == null || !Objects.equals(value, newValue))
		{
			return false;
		}
		
		this.values[index] = newValue;
		return true;
	}
	
	@Override
	public V replace(K key, V newValue)
	{
		if (!checkType(this.type, key))
		{
			return null;
		}
		
		int index = index(key);
		V oldValue = (V) this.values[index];
		if (oldValue == null)
		{
			return null;
		}
		
		this.values[index] = newValue;
		return oldValue;
	}
	
	@Override
	public V removeKey(Object key)
	{
		if (!checkType(this.type, key))
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
		if (!checkType(this.type, key))
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
	public boolean removeValue(Object value)
	{
		boolean removed = false;
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			if (Objects.equals(this.values[i], value))
			{
				this.size--;
				this.values[i] = null;
				removed = true;
			}
		}
		return removed;
	}
	
	@Override
	public void mapValues(BiFunction<? super K, ? super V, ? extends V> mapper)
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
	public <U, R> MutableMap<U, R> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends U, ? extends R>> mapper)
	{
		MutableMap<U, R> map = new ArrayMap<>(this.size);
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			V v = (V) this.values[i];
			if (v != null)
			{
				Entry<? extends U, ? extends R> entry = mapper.apply(this.keys[i], v);
				if (entry != null)
				{
					map.put(entry);
				}
			}
		}
		return map;
	}
	
	@Override
	public <U, R> MutableMap<U, R> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends U, ? extends R>>> mapper)
	{
		MutableMap<U, R> map = new ArrayMap(this.size);
		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			V v = (V) this.values[i];
			if (v != null)
			{
				for (Entry<? extends U, ? extends R> entry : mapper.apply(this.keys[i], v))
				{
					map.put(entry);
				}
			}
		}
		return map;
	}
	
	@Override
	public MutableMap<K, V> copy()
	{
		return new EnumMap<>(this);
	}
	
	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return new EnumMap(this.type);
	}
	
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return new dyvil.collection.immutable.EnumMap<>(this);
	}
}

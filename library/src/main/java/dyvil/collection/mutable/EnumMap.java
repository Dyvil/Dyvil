package dyvil.collection.mutable;

import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.collection.Entry;
import dyvil.collection.ImmutableMap;
import dyvil.collection.MutableMap;
import dyvil.collection.impl.AbstractEnumMap;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;
import dyvil.reflect.types.Type;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromClass
@LiteralConvertible.FromType
@LiteralConvertible.FromArray
public class EnumMap<K extends Enum<K>, V> extends AbstractEnumMap<K, V> implements MutableMap<K, V>
{
	private static final long serialVersionUID = 1734016065128722262L;

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(@NonNull Type<K> type)
	{
		return new EnumMap<>(type.erasure());
	}

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(@NonNull Class<K> type)
	{
		return new EnumMap<>(type);
	}

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> singleton(K key, V value)
	{
		return new EnumMap<>(getKeyType(key), (K[]) new Object[] { key }, (V[]) new Object[] { value }, 1);
	}

	@NonNull
	@SafeVarargs
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(@NonNull Entry<? extends K, ? extends V> @NonNull ... entries)
	{
		return new EnumMap<>(entries);
	}

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> from(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		return new EnumMap<>(entries);
	}

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> from(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> map)
	{
		return new EnumMap<>(map);
	}

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> from(@NonNull AbstractEnumMap<? extends K, ? extends V> map)
	{
		return new EnumMap<K, V>(map);
	}

	@DyvilModifiers(Modifiers.INTERNAL)
	private EnumMap(@NonNull Class<K> type, K @NonNull [] keys, V @NonNull [] values, int size)
	{
		super(type, keys, values, size);
	}

	public EnumMap(@NonNull Class<K> type)
	{
		super(type);
	}

	public EnumMap(@NonNull Type<K> type)
	{
		super(type.erasure());
	}

	public EnumMap(@NonNull Entry<? extends K, ? extends V> @NonNull [] entries)
	{
		super(entries);
	}

	public EnumMap(@NonNull Iterable<? extends @NonNull Entry<? extends K, ? extends V>> map)
	{
		super(map);
	}

	public EnumMap(@NonNull AbstractEnumMap<? extends K, ? extends V> map)
	{
		super(map);
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

	@Nullable
	@Override
	public V put(@NonNull K key, V value)
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

	@Nullable
	@Override
	public V putIfAbsent(@NonNull K key, V value)
	{
		if (!checkType(this.type, key))
		{
			return null;
		}

		final int index = index(key);
		final V thisValue = (V) this.values[index];
		if (thisValue == null)
		{
			this.values[index] = value;
			this.size++;
			return value;
		}
		return thisValue;
	}

	@Override
	public boolean replace(@NonNull K key, V oldValue, V newValue)
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

	@Nullable
	@Override
	public V replace(@NonNull K key, V newValue)
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

	@Nullable
	@Override
	public V removeKey(@NonNull Object key)
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
	public boolean remove(@NonNull Object key, Object value)
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
	public void mapValues(@NonNull BiFunction<? super K, ? super V, ? extends V> mapper)
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
	public void filter(@NonNull BiPredicate<? super K, ? super V> condition)
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

	@NonNull
	@Override
	public <U, R> MutableMap<U, R> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends U, ? extends R>> mapper)
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
	public <U, R> MutableMap<U, R> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends U, ? extends R>>> mapper)
	{
		MutableMap<U, R> map = MutableMap.withCapacity(this.size << 2);
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

	@NonNull
	@Override
	public MutableMap<K, V> copy()
	{
		return this.mutableCopy();
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> immutable()
	{
		return this.immutableCopy();
	}
}

package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.*;
import dyvil.collection.impl.AbstractEnumMap;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.EnumReflection;
import dyvil.reflect.Modifiers;
import dyvil.util.ImmutableException;
import dyvil.reflect.types.Type;

import java.util.Collections;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

@LiteralConvertible.FromArray
@Immutable
public class EnumMap<K extends Enum<K>, V> extends AbstractEnumMap<K, V> implements ImmutableMap<K, V>
{
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
		public void put(@NonNull K key, V value)
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
			EnumMap<K, V> map = new EnumMap<>(this.type, EnumReflection.getEnumConstants(this.type), (V[]) this.values,
			                                  this.size);
			this.size = -1;
			return map;
		}
	}

	private static final long serialVersionUID = -2305035920228304893L;

	// Factory Methods

	@NonNull
	public static <K extends Enum<K>, V> EnumMap<K, V> singleton(@NonNull K key, V value)
	{
		final EnumMap<K, V> result = new EnumMap<>(getKeyType(key));
		result.putInternal(key, value);
		return result;
	}

	@NonNull
	@SafeVarargs
	public static <K extends Enum<K>, V> EnumMap<K, V> apply(@NonNull Entry<K, V> @NonNull ... entries)
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

	@NonNull
	public static <K extends Enum<K>, V> Builder<K, V> builder(@NonNull Type<K> type)
	{
		return new Builder<>(type.erasure());
	}

	@NonNull
	public static <K extends Enum<K>, V> Builder<K, V> builder(@NonNull Class<K> type)
	{
		return new Builder<>(type);
	}

	// Constructors

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

	// Implementation methods

	@Override
	protected void removeAt(int index)
	{
		throw new ImmutableException("Iterator.remove() on Immutable Map");
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(@NonNull K key, V value)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, newSize);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, newSize);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(@NonNull Object key)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, this.size - 1);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(@NonNull Object key, Object value)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, this.size - 1);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, newSize);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, newSize);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, newSize);
	}

	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		int len = this.values.length;
		ImmutableMap.Builder<NK, V> builder = new ArrayMap.Builder<>(this.size);

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

	@NonNull
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
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
		return new EnumMap<>(this.type, this.keys, (NV[]) newValues, this.size);
	}

	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		ImmutableMap.Builder<NK, NV> builder = new ArrayMap.Builder<>(this.size);

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
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		ImmutableMap.Builder<NK, NV> builder = new ArrayMap.Builder<>(this.size);

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

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition)
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
		return new EnumMap<>(this.type, this.keys, (V[]) newValues, newSize);
	}

	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		ImmutableMap.Builder<V, K> builder = new ArrayMap.Builder<>(this.size);

		int len = this.values.length;
		for (int i = 0; i < len; i++)
		{
			builder.put((V) this.values[i], this.keys[i]);
		}
		return builder.build();
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this.immutableCopy();
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutable()
	{
		return this.mutableCopy();
	}

	@Override
	public java.util.@NonNull Map<K, V> toJava()
	{
		return Collections.unmodifiableMap(super.toJava());
	}
}

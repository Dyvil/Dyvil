package dyvil.collection.immutable;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.DyvilModifiers;
import dyvil.annotation.internal.NonNull;
import dyvil.annotation.internal.Nullable;
import dyvil.array.ObjectArray;
import dyvil.collection.*;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.LiteralConvertible;
import dyvil.reflect.Modifiers;
import dyvil.util.Option;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class EmptyMap<K, V> implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = 4719096668028950933L;

	public static final EmptyMap instance = new EmptyMap();

	@SuppressWarnings("unchecked")
	@NonNull
	public static <K, V> EmptyMap<K, V> apply()
	{
		return instance;
	}

	private EmptyMap()
	{
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return EmptyIterator.instance;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public Iterator<K> keyIterator()
	{
		return EmptyIterator.instance;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public Iterator<V> valueIterator()
	{
		return EmptyIterator.instance;
	}

	@Override
	public void forEach(@NonNull Consumer<? super Entry<K, V>> action)
	{
	}

	@Override
	public void forEach(@NonNull BiConsumer<? super K, ? super V> action)
	{
	}

	@Override
	public boolean containsKey(Object key)
	{
		return false;
	}

	@Override
	public boolean contains(Object key, Object value)
	{
		return false;
	}

	@Override
	public boolean containsValue(Object value)
	{
		return false;
	}

	@Override
	public V get(Object key)
	{
		return null;
	}

	@Nullable
	@Override
	public Entry<K, V> getEntry(Object key)
	{
		return null;
	}

	@NonNull
	@Override
	public Option<V> getOption(Object key)
	{
		return Option.apply();
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		return SingletonMap.apply(key, value);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		return (ImmutableMap<K, V>) map.immutable();
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		return this;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
	{
		return this;
	}

	@NonNull
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		return (ImmutableMap<NK, V>) this;
	}

	@NonNull
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		return (ImmutableMap<K, NV>) this;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		return (ImmutableMap<NK, NV>) this;
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		return (ImmutableMap<NK, NV>) this;
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> predicate)
	{
		return this;
	}

	@SuppressWarnings("unchecked")
	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		return (ImmutableMap) this;
	}

	@Override
	public Entry<K, V> @NonNull [] toArray()
	{
		return (Entry<K, V>[]) new Entry[0];
	}

	@Override
	public void toArray(int index, @NonNull Entry<K, V> @NonNull [] store)
	{
	}

	@Override
	public Object @NonNull [] toKeyArray()
	{
		return ObjectArray.EMPTY;
	}

	@Override
	public void toKeyArray(int index, Object @NonNull [] store)
	{
	}

	@Override
	public Object @NonNull [] toValueArray()
	{
		return ObjectArray.EMPTY;
	}

	@Override
	public void toValueArray(int index, Object @NonNull [] store)
	{
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> copy()
	{
		return this;
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return MutableMap.apply();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return MutableMap.withCapacity(capacity);
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutable()
	{
		return MutableMap.apply();
	}

	@Override
	public <RK, RV> Builder<RK, RV> immutableBuilder()
	{
		return ImmutableMap.builder();
	}

	@Override
	public <RK, RV> Builder<RK, RV> immutableBuilder(int capacity)
	{
		return ImmutableMap.builder(capacity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.EMPTY_MAP;
	}

	@NonNull
	@Override
	public String toString()
	{
		return Map.EMPTY_STRING;
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

	@NonNull
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}

	@NonNull
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}

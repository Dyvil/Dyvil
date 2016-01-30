package dyvil.collection.immutable;

import dyvil.annotation._internal.DyvilModifiers;
import dyvil.array.ObjectArray;
import dyvil.collection.*;
import dyvil.collection.iterator.EmptyIterator;
import dyvil.lang.literal.NilConvertible;
import dyvil.reflect.Modifiers;
import dyvil.annotation.Immutable;
import dyvil.util.None;
import dyvil.util.Option;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@NilConvertible
@DyvilModifiers(Modifiers.OBJECT_CLASS)
@Immutable
public final class EmptyMap<K, V> implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = 4719096668028950933L;
	
	public static final EmptyMap instance = new EmptyMap();
	
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
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return EmptyIterator.instance;
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return EmptyIterator.instance;
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return EmptyIterator.instance;
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
	{
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
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
	
	@Override
	public Option<V> getOption(Object key)
	{
		return None.instance;
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return SingletonMap.apply(key, value);
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		return (ImmutableMap<K, V>) map.immutable();
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		return this;
	}
	
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		return (ImmutableMap<NK, V>) this;
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		return (ImmutableMap<K, NV>) this;
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		return (ImmutableMap<NK, NV>) this;
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		return (ImmutableMap<NK, NV>) this;
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return this;
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		return (ImmutableMap) this;
	}
	
	@Override
	public Entry<K, V>[] toArray()
	{
		return new Entry[0];
	}
	
	@Override
	public void toArray(int index, Entry<K, V>[] store)
	{
	}
	
	@Override
	public Object[] toKeyArray()
	{
		return ObjectArray.EMPTY;
	}
	
	@Override
	public void toKeyArray(int index, Object[] store)
	{
	}
	
	@Override
	public Object[] toValueArray()
	{
		return ObjectArray.EMPTY;
	}
	
	@Override
	public void toValueArray(int index, Object[] store)
	{
	}
	
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
		return MutableMap.apply(capacity);
	}
	
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
		return null;
	}
	
	@Override
	public java.util.Map<K, V> toJava()
	{
		return Collections.EMPTY_MAP;
	}
	
	@Override
	public String toString()
	{
		return "[]";
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
	
	private Object writeReplace() throws java.io.ObjectStreamException
	{
		return instance;
	}
	
	private Object readResolve() throws java.io.ObjectStreamException
	{
		return instance;
	}
}

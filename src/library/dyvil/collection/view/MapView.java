package dyvil.collection.view;

import dyvil.annotation.Immutable;
import dyvil.annotation.internal.NonNull;
import dyvil.collection.*;
import dyvil.collection.iterator.ImmutableIterator;
import dyvil.util.Option;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

@Immutable
public class MapView<K, V> implements ImmutableMap<K, V>
{
	private static final long serialVersionUID = 1586369703282366862L;

	protected final Map<K, V> map;

	public MapView(Map<K, V> map)
	{
		this.map = map;
	}

	@Override
	public int size()
	{
		return this.map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.map.isEmpty();
	}

	@NonNull
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return this.map.isImmutable() ? this.map.iterator() : new ImmutableIterator(this.map.iterator());
	}

	@NonNull
	@Override
	public Iterator<K> keyIterator()
	{
		return this.map.isImmutable() ? this.map.keyIterator() : new ImmutableIterator(this.map.keyIterator());
	}

	@NonNull
	@Override
	public Iterator<V> valueIterator()
	{
		return this.map.isImmutable() ? this.map.valueIterator() : new ImmutableIterator(this.map.valueIterator());
	}

	@Override
	public void forEach(@NonNull BiConsumer<? super K, ? super V> action)
	{
		this.map.forEach(action);
	}

	@Override
	public void forEach(@NonNull Consumer<? super Entry<K, V>> action)
	{
		this.map.forEach(action);
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.map.containsValue(value);
	}

	@Override
	public boolean contains(Object key, Object value)
	{
		return this.map.contains(key, value);
	}

	@Override
	public V get(Object key)
	{
		return this.map.get(key);
	}

	@Override
	public Entry<K, V> getEntry(Object key)
	{
		return this.map.getEntry(key);
	}

	@NonNull
	@Override
	public Option<V> getOption(Object key)
	{
		return this.map.getOption(key);
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> withEntry(K key, V value)
	{
		return new MapView(this.map.withEntry(key, value));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> union(@NonNull Map<? extends K, ? extends V> map)
	{
		return new MapView(this.map.union(map));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyRemoved(Object key)
	{
		return new MapView(this.map.keyRemoved(key));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> removed(Object key, Object value)
	{
		return new MapView(this.map.removed(key, value));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> valueRemoved(Object value)
	{
		return new MapView(this.map.valueRemoved(value));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> difference(@NonNull Map<?, ?> map)
	{
		return new MapView(this.map.difference(map));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> keyDifference(@NonNull Collection<?> keys)
	{
		return new MapView(this.map.keyDifference(keys));
	}

	@NonNull
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(@NonNull BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		return new MapView(this.map.keyMapped(mapper));
	}

	@NonNull
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(@NonNull BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		return new MapView(this.map.valueMapped(mapper));
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Entry<? extends NK, ? extends NV>> mapper)
	{
		return new MapView(this.map.entryMapped(mapper));
	}

	@NonNull
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(@NonNull BiFunction<? super K, ? super V, ? extends @NonNull Iterable<? extends @NonNull Entry<? extends NK, ? extends NV>>> mapper)
	{
		return new MapView(this.map.flatMapped(mapper));
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> filtered(@NonNull BiPredicate<? super K, ? super V> condition)
	{
		return new MapView(this.map.filtered(condition));
	}

	@NonNull
	@Override
	public ImmutableMap<V, K> inverted()
	{
		return new MapView(this.map.inverted());
	}

	@NonNull
	@Override
	public ImmutableMap<K, V> copy()
	{
		return new MapView(this.map.copy());
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy()
	{
		return this.map.emptyCopy();
	}

	@Override
	public <RK, RV> MutableMap<RK, RV> emptyCopy(int capacity)
	{
		return this.map.emptyCopy(capacity);
	}

	@NonNull
	@Override
	public MutableMap<K, V> mutable()
	{
		return this.map.mutable();
	}

	@Override
	public <RK, RV> Builder<RK, RV> immutableBuilder()
	{
		return this.map.immutableBuilder();
	}

	@Override
	public <RK, RV> Builder<RK, RV> immutableBuilder(int capacity)
	{
		return this.map.immutableBuilder(capacity);
	}

	@Override
	public java.util.@NonNull Map<K, V> toJava()
	{
		return this.map.isImmutable() ? this.map.toJava() : Collections.unmodifiableMap(this.map.toJava());
	}

	@NonNull
	@Override
	public String toString()
	{
		return "view " + this.map.toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		return this.map.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return this.map.hashCode();
	}
}

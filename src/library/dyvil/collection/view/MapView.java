package dyvil.collection.view;

import dyvil.collection.*;
import dyvil.collection.iterator.ImmutableIterator;
import dyvil.annotation.Immutable;
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
	
	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return this.map.isImmutable() ? this.map.iterator() : new ImmutableIterator(this.map.iterator());
	}
	
	@Override
	public Iterator<K> keyIterator()
	{
		return this.map.isImmutable() ? this.map.keyIterator() : new ImmutableIterator(this.map.keyIterator());
	}
	
	@Override
	public Iterator<V> valueIterator()
	{
		return this.map.isImmutable() ? this.map.valueIterator() : new ImmutableIterator(this.map.valueIterator());
	}
	
	@Override
	public void forEach(BiConsumer<? super K, ? super V> action)
	{
		this.map.forEach(action);
	}
	
	@Override
	public void forEach(Consumer<? super Entry<K, V>> action)
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
	public Option<V> getOption(Object key)
	{
		return this.map.getOption(key);
	}
	
	@Override
	public ImmutableMap<K, V> $plus(K key, V value)
	{
		return new MapView(this.map.$plus(key, value));
	}
	
	@Override
	public ImmutableMap<K, V> $plus$plus(Map<? extends K, ? extends V> map)
	{
		return new MapView(this.map.$plus$plus(map));
	}
	
	@Override
	public ImmutableMap<K, V> $minus$at(Object key)
	{
		return new MapView(this.map.$minus$at(key));
	}
	
	@Override
	public ImmutableMap<K, V> $minus(Object key, Object value)
	{
		return new MapView(this.map.$minus(key, value));
	}
	
	@Override
	public ImmutableMap<K, V> $minus$colon(Object value)
	{
		return new MapView(this.map.$minus$colon(value));
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Map<?, ?> map)
	{
		return new MapView(this.map.$minus$minus(map));
	}
	
	@Override
	public ImmutableMap<K, V> $minus$minus(Collection<?> keys)
	{
		return new MapView(this.map.$minus$minus(keys));
	}
	
	@Override
	public <NK> ImmutableMap<NK, V> keyMapped(BiFunction<? super K, ? super V, ? extends NK> mapper)
	{
		return new MapView(this.map.keyMapped(mapper));
	}
	
	@Override
	public <NV> ImmutableMap<K, NV> valueMapped(BiFunction<? super K, ? super V, ? extends NV> mapper)
	{
		return new MapView(this.map.valueMapped(mapper));
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> entryMapped(BiFunction<? super K, ? super V, ? extends Entry<? extends NK, ? extends NV>> mapper)
	{
		return new MapView(this.map.entryMapped(mapper));
	}
	
	@Override
	public <NK, NV> ImmutableMap<NK, NV> flatMapped(BiFunction<? super K, ? super V, ? extends Iterable<? extends Entry<? extends NK, ? extends NV>>> mapper)
	{
		return new MapView(this.map.flatMapped(mapper));
	}
	
	@Override
	public ImmutableMap<K, V> filtered(BiPredicate<? super K, ? super V> condition)
	{
		return new MapView(this.map.filtered(condition));
	}
	
	@Override
	public ImmutableMap<V, K> inverted()
	{
		return new MapView(this.map.inverted());
	}
	
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
	public java.util.Map<K, V> toJava()
	{
		return this.map.isImmutable() ? this.map.toJava() : Collections.unmodifiableMap(this.map.toJava());
	}
	
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

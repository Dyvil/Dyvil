package dyvil.maps.mutable;

import java.util.Map;

import dyvil.lang.tuple.Tuple2;

public interface DMap<K, V> extends Map<K, V>
{
	@Override
	public V put(K key, V value);
	
	public V $plus(Tuple2<K, V> entry);
	
	@Override
	public V remove(Object key);
	
	public V $minus(Object key);
	
	public V apply(K key);
}

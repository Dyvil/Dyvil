package dyvil.maps;

import java.util.LinkedList;
import java.util.Map;

import dyvil.lang.tuple.Tuple2;

public class LinkedListMap<K, V> extends AbstractListMap<K, V>
{
	public LinkedListMap()
	{
		this.entries = new LinkedList();
	}
	
	public LinkedListMap(Map<? extends K, ? extends V> m)
	{
		this();
		this.putAll(m);
	}
	
	@Override
	public V $plus(Tuple2<K, V> entry)
	{
		return super.put(entry._1, entry._2);
	}
	
	@Override
	public V $minus(Object key)
	{
		return super.remove(key);
	}
	
	@Override
	public V apply(K key)
	{
		return super.get(key);
	}
}

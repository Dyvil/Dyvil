package dyvil.maps;

import java.util.ArrayList;
import java.util.Map;

import dyvil.lang.tuple.Tuple2;

public class ArrayListMap<K, V> extends AbstractListMap<K, V>
{
	public ArrayListMap()
	{
		this(16);
	}
	
	public ArrayListMap(int capacity)
	{
		this.entries = new ArrayList(capacity);
	}
	
	public ArrayListMap(Map<? extends K, ? extends V> m)
	{
		this(m.size());
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
}

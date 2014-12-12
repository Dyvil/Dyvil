package dyvil.maps;

import java.util.ArrayList;
import java.util.Map;

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
}

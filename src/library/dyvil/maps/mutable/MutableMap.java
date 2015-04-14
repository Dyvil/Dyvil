package dyvil.maps.mutable;

import dyvil.lang.Map;

public interface MutableMap<K, V> extends Map<K, V>
{	
	@Override
	public default MutableMap<K, V> mutable()
	{
		return this;
	}
}

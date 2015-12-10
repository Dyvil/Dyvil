package dyvil.tools.dpf.ast;

import dyvil.collection.Collection;
import dyvil.collection.Map;
import dyvil.tuple.Tuple2;

public interface Expandable
{
	Object expand(Map<String, Object> mappings, boolean mutate);

	static Object expand(Object object, Map<String, Object> mappings, boolean mutate)
	{
		if (object instanceof Expandable)
		{
			return ((Expandable) object).expand(mappings, mutate);
		}
		if (object instanceof Collection)
		{
			Collection<? super Object> collection = (Collection<? super Object>) object;
			return expandCollection(collection, mappings, mutate);
		}
		if (object instanceof Map)
		{
			return expandMap((Map<?, ? super Object>) object, mappings, mutate);
		}
		return object;
	}

	static Collection<?> expandCollection(Collection<?> collection, Map<String, Object> mappings, boolean mutate)
	{
		if (!mutate || collection.isImmutable())
		{
			return collection.mapped(e -> expand(e, mappings, mutate));
		}
		((Collection) collection).map(e -> expand(e, mappings, true));
		return collection;
	}

	static Map<?, ?> expandMap(Map<?, ?> map, Map<String, Object> mappings, boolean mutate)
	{
		if (!mutate || map.isImmutable())
		{
			return map.entryMapped((k, v) -> new Tuple2(expand(k, mappings, mutate), expand(v, mappings, mutate)));
		}
		map.mapEntries((k, v) -> new Tuple2(expand(k, mappings, true), expand(v, mappings, true)));
		return map;
	}
}

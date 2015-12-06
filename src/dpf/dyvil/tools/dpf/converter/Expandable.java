package dyvil.tools.dpf.converter;

import dyvil.collection.Collection;
import dyvil.collection.Map;

public interface Expandable
{
	Object expand(Map<String, Object> mappings, boolean mutate);

	static Object expand(Object o, Map<String, Object> mappings, boolean mutate)
	{
		if (o instanceof Expandable)
		{
			return ((Expandable) o).expand(mappings, mutate);
		}
		if (o instanceof Collection)
		{
			Collection<? super Object> collection = (Collection<? super Object>) o;
			if (!mutate || collection.isImmutable())
			{
				return collection.mapped(e -> expand(e, mappings, mutate));
			}
			collection.map(e -> expand(e, mappings, true));
			return o;
		}
		if (o instanceof Map)
		{
			Map<?, ? super Object> map = (Map<?, ? super Object>) o;
			if (!mutate || map.isImmutable())
			{
				return map.valueMapped((k, v) -> expand(v, mappings, mutate));
			}
			map.mapValues((k, v) -> expand(v, mappings, true));
		}
		return o;
	}
}

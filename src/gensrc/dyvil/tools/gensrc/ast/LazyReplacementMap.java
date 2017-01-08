package dyvil.tools.gensrc.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazyReplacementMap implements ReplacementMap
{
	private Map<String, String> store;

	private List<ReplacementMap> imports;

	private final ReplacementMap parent;

	public LazyReplacementMap(ReplacementMap parent)
	{
		this.parent = parent;
	}

	public void define(String key, String value)
	{
		this.createStore().put(key, value);
	}

	public void undefine(String key)
	{
		this.createStore().put(key, null);
	}

	public void remove(String key)
	{
		if (this.store != null)
		{
			this.store.remove(key);
		}
	}

	private Map<String, String> createStore()
	{
		if (this.store == null)
		{
			return this.store = new HashMap<>();
		}
		return this.store;
	}

	public void importFrom(ReplacementMap map)
	{
		if (this.imports == null)
		{
			this.imports = new ArrayList<>();
		}

		this.imports.add(map);
	}

	@Override
	public String getReplacement(String key)
	{
		if (this.store != null && this.store.containsKey(key))
		{
			return this.store.get(key);
		}
		if (this.imports != null)
		{
			for (ReplacementMap map : this.imports)
			{
				final String replacement = map.getReplacement(key);
				if (replacement != null)
				{
					return replacement;
				}
			}
		}
		return this.getParent(key);
	}

	public String getParent(String key)
	{
		return this.parent == null ? null : this.parent.getReplacement(key);
	}
}

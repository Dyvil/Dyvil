package dyvilx.tools.gensrc.ast.scope;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LazyScope implements Scope
{
	private Map<String, String> store;

	private List<Scope> imports;

	private final Scope parent;

	public LazyScope(Scope parent)
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

	public void importFrom(Scope map)
	{
		if (this.imports == null)
		{
			this.imports = new ArrayList<>();
		}

		this.imports.add(map);
	}

	@Override
	public File getSourceFile()
	{
		return this.parent.getSourceFile();
	}

	@Override
	public Scope getGlobalParent()
	{
		return this.parent.getGlobalParent();
	}

	@Override
	public String getString(String key)
	{
		if (this.store != null && this.store.containsKey(key))
		{
			return this.store.get(key);
		}
		if (this.imports != null)
		{
			for (Scope map : this.imports)
			{
				final String replacement = map.getString(key);
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
		return this.parent == null ? null : this.parent.getString(key);
	}
}

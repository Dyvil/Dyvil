package dyvil.tools.dpf.converter;

import dyvil.collection.Map;
import dyvil.tools.dpf.ast.Expandable;

public class NameAccess extends DyvilValueVisitor implements Expandable
{
	private Object value;
	private String name;
	
	public NameAccess(String name)
	{
		this.name = name;
	}
	
	@Override
	protected void visitObject(Object o)
	{
		this.value = o;
	}
	
	@Override
	public Object expand(Map<String, Object> mappings, boolean mutate)
	{
		String key;
		if (this.value == null)
		{
			key = this.name;
		}
		else
		{
			key = this.value.toString() + '.' + this.name;
		}
		return mappings.get(key);
	}

	@Override
	public String toString()
	{
		if (this.value == null)
		{
			return this.name;
		}
		return this.value.toString() + '.' + this.name;
	}
}

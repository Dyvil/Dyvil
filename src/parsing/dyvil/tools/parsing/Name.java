package dyvil.tools.parsing;

import dyvil.collection.Map;
import dyvil.collection.mutable.HashMap;
import dyvil.tools.parsing.lexer.BaseSymbols;

public final class Name
{
	private static final Map<String, Name> map = new HashMap();
	
	public final String	qualified;
	public final String	unqualified;
	
	public Name(String name)
	{
		this.qualified = this.unqualified = name;
		map.put(name, this);
	}
	
	public Name(String unqualified, String qualified)
	{
		this.qualified = qualified;
		this.unqualified = unqualified;
		
		map.put(qualified, this);
		map.put(unqualified, this);
	}
	
	public static Name get(String unqualified, String qualified)
	{
		Name name = map.get(qualified);
		if (name != null)
		{
			return name;
		}
		
		name = map.get(unqualified);
		if (name != null)
		{
			return name;
		}
		
		return new Name(unqualified, qualified);
	}
	
	public static Name get(String value)
	{
		Name name = map.get(value);
		if (name != null)
		{
			return name;
		}
		
		return new Name(BaseSymbols.unqualify(value), BaseSymbols.qualify(value));
	}
	
	public static Name getSpecial(String value)
	{
		Name name = map.get(value);
		if (name != null)
		{
			return name;
		}
		
		return new Name(value, BaseSymbols.qualify(value));
	}
	
	public static Name getQualified(String value)
	{
		Name name = map.get(value);
		if (name != null)
		{
			return name;
		}
		
		return new Name(value);
	}
	
	public boolean equals(String name)
	{
		return this.qualified.equals(name);
	}
	
	public boolean endsWith(String name)
	{
		return this.qualified.endsWith(name);
	}
	
	@Override
	public String toString()
	{
		return this.unqualified;
	}
}

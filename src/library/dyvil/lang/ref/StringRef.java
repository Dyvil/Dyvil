package dyvil.lang.ref;

import dyvil.lang.literal.StringConvertible;

@StringConvertible
public class StringRef implements StringRef$, ObjectRef$<String>
{
	public String	value;
	
	public static StringRef apply(String value)
	{
		return new StringRef(value);
	}
	
	public StringRef(String value)
	{
		this.value = value;
	}
	
	@Override
	public String apply()
	{
		return this.value;
	}
	
	@Override
	public void update(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value;
	}
}

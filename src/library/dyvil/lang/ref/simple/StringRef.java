package dyvil.lang.ref.simple;

import dyvil.lang.literal.StringConvertible;
import dyvil.lang.ref.IStringRef;

@StringConvertible
public class StringRef implements IStringRef
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
	public String get()
	{
		return this.value;
	}
	
	@Override
	public void set(String value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value;
	}
}

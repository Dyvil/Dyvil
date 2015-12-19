package dyvil.lang.ref.simple;

import dyvil.lang.literal.StringConvertible;
import dyvil.lang.ref.StringRef;

@StringConvertible
public class SimpleStringRef implements StringRef
{
	public String value;
	
	public static SimpleStringRef apply(String value)
	{
		return new SimpleStringRef(value);
	}
	
	public SimpleStringRef(String value)
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
}

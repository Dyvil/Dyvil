package dyvil.lang.text;

import dyvil.lang.String;

public class StringRef extends dyvil.lang.String
{
	protected StringRef(char[] value)
	{
		super(value);
	}
	
	@Override
	public String $set(char[] value)
	{
		this.value = value;
		return this;
	}
}

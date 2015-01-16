package dyvil.lang.text;

import dyvil.lang.String;

public class StringConst extends dyvil.lang.String
{
	protected StringConst(char[] value)
	{
		super(value);
	}
	
	@Override
	public String $eq(char[] value)
	{
		return new StringConst(value);
	}
}

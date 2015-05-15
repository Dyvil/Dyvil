package dyvil.lang.ref;

import dyvil.lang.literal.BooleanConvertible;

@BooleanConvertible
public class BooleanRef implements BooleanRef$
{
	public boolean	value;
	
	public static BooleanRef apply(boolean value)
	{
		return new BooleanRef(value);
	}
	
	public BooleanRef(boolean value)
	{
		this.value = value;
	}
	
	@Override
	public boolean apply()
	{
		return this.value;
	}
	
	@Override
	public void update(boolean value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value ? "true" : "false";
	}
}

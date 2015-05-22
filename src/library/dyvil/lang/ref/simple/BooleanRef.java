package dyvil.lang.ref.simple;

import dyvil.lang.literal.BooleanConvertible;
import dyvil.lang.ref.IBooleanRef;

@BooleanConvertible
public class BooleanRef implements IBooleanRef
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
	public boolean get()
	{
		return this.value;
	}
	
	@Override
	public void set(boolean value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value ? "true" : "false";
	}
}

package dyvil.lang.ref.simple;

import dyvil.lang.literal.BooleanConvertible;
import dyvil.lang.ref.BooleanRef;

@BooleanConvertible
public class SimpleBooleanRef implements BooleanRef
{
	public boolean value;
	
	public static SimpleBooleanRef apply(boolean value)
	{
		return new SimpleBooleanRef(value);
	}
	
	public SimpleBooleanRef(boolean value)
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
}

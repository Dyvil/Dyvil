package dyvil.lang.ref.simple;

import dyvil.lang.literal.IntConvertible;
import dyvil.lang.ref.IShortRef;

@IntConvertible
public class ShortRef implements IShortRef
{
	public short	value;
	
	public static ShortRef apply(short value)
	{
		return new ShortRef(value);
	}
	
	public ShortRef(short value)
	{
		this.value = value;
	}
	
	@Override
	public short get()
	{
		return this.value;
	}
	
	@Override
	public void set(short value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Short.toString(this.value);
	}
}

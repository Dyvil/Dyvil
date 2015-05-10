package dyvil.lang.ref;

import dyvil.lang.literal.IntConvertible;

@IntConvertible
public class ShortRef implements ShortRef$
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
	public short apply()
	{
		return this.value;
	}
	
	@Override
	public void update(short value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Short.toString(this.value);
	}
}

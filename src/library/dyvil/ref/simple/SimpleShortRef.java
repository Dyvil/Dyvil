package dyvil.ref.simple;

import dyvil.lang.literal.IntConvertible;
import dyvil.ref.ShortRef;
import dyvil.ref.ShortRef;

@IntConvertible
public class SimpleShortRef implements ShortRef
{
	public short value;
	
	public static SimpleShortRef apply(short value)
	{
		return new SimpleShortRef(value);
	}
	
	public SimpleShortRef(short value)
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
}

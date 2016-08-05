package dyvil.ref.simple;

import dyvil.lang.LiteralConvertible;
import dyvil.ref.ShortRef;

@LiteralConvertible.FromInt
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

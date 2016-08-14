package dyvil.ref.simple;

import dyvil.lang.LiteralConvertible;
import dyvil.ref.IntRef;

@LiteralConvertible.FromInt
public class SimpleIntRef implements IntRef
{
	public int value;
	
	public static SimpleIntRef apply(int value)
	{
		return new SimpleIntRef(value);
	}
	
	public SimpleIntRef(int value)
	{
		this.value = value;
	}
	
	@Override
	public int get()
	{
		return this.value;
	}
	
	@Override
	public void set(int value)
	{
		this.value = value;
	}
}

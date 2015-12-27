package dyvil.ref.simple;

import dyvil.lang.literal.IntConvertible;
import dyvil.ref.IntRef;

@IntConvertible
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

package dyvil.lang.ref.simple;

import dyvil.lang.literal.CharConvertible;
import dyvil.lang.ref.CharRef;

@CharConvertible
public class SimpleCharRef implements CharRef
{
	public char	value;
	
	public static SimpleCharRef apply(char value)
	{
		return new SimpleCharRef(value);
	}
	
	public SimpleCharRef(char value)
	{
		this.value = value;
	}
	
	@Override
	public char get()
	{
		return this.value;
	}
	
	@Override
	public void set(char value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Character.toString(this.value);
	}
}

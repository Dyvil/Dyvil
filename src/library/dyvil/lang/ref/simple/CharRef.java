package dyvil.lang.ref.simple;

import dyvil.lang.literal.CharConvertible;
import dyvil.lang.ref.ICharRef;

@CharConvertible
public class CharRef implements ICharRef
{
	public char	value;
	
	public static CharRef apply(char value)
	{
		return new CharRef(value);
	}
	
	public CharRef(char value)
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

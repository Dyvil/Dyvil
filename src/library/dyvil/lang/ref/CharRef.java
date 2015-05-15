package dyvil.lang.ref;

import dyvil.lang.literal.CharConvertible;

@CharConvertible
public class CharRef implements CharRef$
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
	public char apply()
	{
		return this.value;
	}
	
	@Override
	public void update(char value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return Character.toString(this.value);
	}
}

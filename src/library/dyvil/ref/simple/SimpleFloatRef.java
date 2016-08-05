package dyvil.ref.simple;

import dyvil.lang.LiteralConvertible;
import dyvil.ref.FloatRef;

@LiteralConvertible.FromFloat
public class SimpleFloatRef implements FloatRef
{
	public float value;
	
	public static SimpleFloatRef apply(float value)
	{
		return new SimpleFloatRef(value);
	}
	
	public SimpleFloatRef(float value)
	{
		this.value = value;
	}
	
	@Override
	public float get()
	{
		return this.value;
	}
	
	@Override
	public void set(float value)
	{
		this.value = value;
	}
}

package dyvil.lang.ref.simple;

import dyvil.lang.literal.FloatConvertible;
import dyvil.lang.ref.FloatRef;

@FloatConvertible
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

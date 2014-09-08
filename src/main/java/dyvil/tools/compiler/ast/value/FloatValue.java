package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class FloatValue implements IValue
{
	public float	value;
	
	public FloatValue(String value)
	{
		this.value = Float.parseFloat(value);
	}
	
	public FloatValue(float value)
	{
		this.value = value;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return Type.FLOAT;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

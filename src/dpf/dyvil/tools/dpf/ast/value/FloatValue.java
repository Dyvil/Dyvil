package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;

public class FloatValue implements Value
{
	protected float value;
	
	public FloatValue(float value)
	{
		this.value = value;
	}
	
	public float getValue()
	{
		return this.value;
	}
	
	public void setValue(float value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		visitor.visitFloat(this.value);
	}
	
	@Override
	public String toString()
	{
		return this.value + "F";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('F');
	}
}

package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;

public class DoubleValue implements Constant
{
	protected double value;
	
	public DoubleValue(double value)
	{
		this.value = value;
	}
	
	public double getValue()
	{
		return this.value;
	}
	
	public void setValue(double value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		visitor.visitDouble(this.value);
	}

	@Override
	public Object toObject()
	{
		return dyvil.lang.Double.apply(this.value);
	}

	@Override
	public void appendString(StringBuilder builder)
	{
		builder.append(this.value);
	}

	@Override
	public String toString()
	{
		return this.value + "D";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('D');
	}
}

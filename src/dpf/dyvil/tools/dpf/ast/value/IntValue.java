package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;

public class IntValue implements Constant
{
	protected int value;
	
	public IntValue(int value)
	{
		this.value = value;
	}
	
	public int getValue()
	{
		return this.value;
	}
	
	public void setValue(int value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		visitor.visitInt(this.value);
	}
	
	@Override
	public Object toObject()
	{
		return dyvil.lang.Int.apply(this.value);
	}

	@Override
	public void appendString(StringBuilder builder)
	{
		builder.append(this.value);
	}

	@Override
	public String toString()
	{
		return Integer.toString(this.value);
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

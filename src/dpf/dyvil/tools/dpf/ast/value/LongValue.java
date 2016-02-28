package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;

public class LongValue implements Constant
{
	protected long value;
	
	public LongValue(long value)
	{
		this.value = value;
	}
	
	public long getValue()
	{
		return this.value;
	}
	
	public void setValue(long value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		visitor.visitLong(this.value);
	}

	@Override
	public Object toObject()
	{
		return this.value;
	}

	@Override
	public void appendString(StringBuilder builder)
	{
		builder.append(this.value);
	}

	@Override
	public String toString()
	{
		return this.value + "L";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value).append('L');
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || !(o instanceof LongValue))
		{
			return false;
		}

		final LongValue that = (LongValue) o;
		return this.value == that.value;
	}

	@Override
	public int hashCode()
	{
		return (int) (this.value ^ (this.value >>> 32));
	}
}

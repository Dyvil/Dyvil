package dyvil.tools.dpf.ast.value;

public class DoubleValue implements Value
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

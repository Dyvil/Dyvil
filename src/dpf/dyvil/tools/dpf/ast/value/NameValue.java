package dyvil.tools.dpf.ast.value;

import dyvil.tools.parsing.Name;

public class NameValue implements Value
{
	protected Name value;
	
	public NameValue(Name value)
	{
		this.value = value;
	}
	
	public Name getValue()
	{
		return this.value;
	}
	
	public void setValue(Name value)
	{
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.value.toString();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.value);
	}
}

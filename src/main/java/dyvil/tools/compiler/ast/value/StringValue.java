package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class StringValue implements IValue
{
	public String value;
	
	public StringValue(String value)
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
		return Type.STRING;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append('"').append(this.value).append('"');
	}
}

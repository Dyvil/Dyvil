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
	public boolean isConstant()
	{
		return true;
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
}

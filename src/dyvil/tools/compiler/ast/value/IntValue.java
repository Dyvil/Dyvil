package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class IntValue implements IValue
{
	public int value;
	
	public IntValue(String value)
	{
		this.value = Integer.parseInt(value);
	}
	
	public IntValue(String value, int radix)
	{
		this.value = Integer.parseInt(value);
	}
	
	public IntValue(int value)
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
		return Type.INT;
	}
}

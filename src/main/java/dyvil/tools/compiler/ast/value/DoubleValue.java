package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class DoubleValue implements IValue
{
	public double value;
	
	public DoubleValue(String value)
	{
		this.value = Double.parseDouble(value);
	}
	
	public DoubleValue(double value)
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
		return Type.DOUBLE;
	}
}

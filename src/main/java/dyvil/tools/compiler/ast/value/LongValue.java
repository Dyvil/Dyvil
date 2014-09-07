package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class LongValue implements IValue
{
	public long value;
	
	public LongValue(String value)
	{
		this.value = Long.parseLong(value);
	}
	
	public LongValue(String value, int radix)
	{
		this.value = Long.parseLong(value);
	}
	
	public LongValue(long value)
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
		return Type.LONG;
	}
}

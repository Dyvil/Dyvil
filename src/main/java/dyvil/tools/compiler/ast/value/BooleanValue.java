package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class BooleanValue implements IValue
{
	private static BooleanValue TRUE = new BooleanValue(true);
	private static BooleanValue FALSE = new BooleanValue(false);
	
	public boolean value;
	
	public static BooleanValue of(boolean value)
	{
		return value ? TRUE : FALSE;
	}
	
	private BooleanValue(boolean value)
	{
		this.value = value;
	}
	
	@Override
	public boolean isConstant()
	{
		return false;
	}
	
	@Override
	public IValue fold()
	{
		return this;
	}
	
	@Override
	public Type getType()
	{
		return Type.BOOL;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(prefix).append(this.value);
	}
}

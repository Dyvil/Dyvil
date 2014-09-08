package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class BooleanValue implements IValue
{
	public static BooleanValue TRUE = new BooleanValue(true);
	public static BooleanValue FALSE = new BooleanValue(false);
	
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
		buffer.append(this.value);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.value ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BooleanValue))
			return false;
		BooleanValue other = (BooleanValue) obj;
		if (this.value != other.value)
			return false;
		return true;
	}
}

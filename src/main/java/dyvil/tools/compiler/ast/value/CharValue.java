package dyvil.tools.compiler.ast.value;

import dyvil.tools.compiler.ast.type.Type;

public class CharValue implements IValue
{
	public char value;
	
	public CharValue(String value)
	{
		this.value = value.charAt(0);
	}
	
	public CharValue(char value)
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
		return Type.CHAR;
	}
}

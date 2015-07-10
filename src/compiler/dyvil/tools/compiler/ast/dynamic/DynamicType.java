package dyvil.tools.compiler.ast.dynamic;

import dyvil.tools.compiler.ast.type.UnknownType;

public final class DynamicType extends UnknownType
{
	@Override
	public int typeTag()
	{
		return DYNAMIC;
	}
	
	@Override
	public String toString()
	{
		return "dynamic";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("dynamic");
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return this == obj;
	}
	
	@Override
	public int hashCode()
	{
		return DYNAMIC;
	}
}

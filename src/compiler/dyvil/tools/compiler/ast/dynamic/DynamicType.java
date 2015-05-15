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
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("dynamic");
	}
}

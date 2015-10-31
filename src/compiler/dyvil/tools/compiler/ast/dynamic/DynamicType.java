package dyvil.tools.compiler.ast.dynamic;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.MethodMatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.UnknownType;
import dyvil.tools.parsing.Name;

public final class DynamicType extends UnknownType
{
	@Override
	public int typeTag()
	{
		return DYNAMIC;
	}
	
	@Override
	public void getMethodMatches(MethodMatchList list, IValue instance, Name name, IArguments arguments)
	{
		list.add(new DynamicMethod(name), 1);
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

package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;

public class LambdaType extends Type implements ITyped, ITypeList
{
	public Type			returnType;
	public List<Type>	argumentTypes;
	
	public LambdaType()
	{
		this.argumentTypes = new ArrayList(2);
	}
	
	public LambdaType(TupleType tupleType)
	{
		this.argumentTypes = tupleType.types;
	}
	
	@Override
	public void setType(Type type)
	{
		this.returnType = type;
	}
	
	@Override
	public Type getType()
	{
		return this.returnType;
	}
	
	@Override
	public void setTypes(List<Type> types)
	{
		this.argumentTypes = types;
	}
	
	@Override
	public List<Type> getTypes()
	{
		return this.argumentTypes;
	}
	
	@Override
	public void addType(Type type)
	{
		this.argumentTypes.add(type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(this.argumentTypes, buffer, true);
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.returnType.toString("", buffer);
	}
}

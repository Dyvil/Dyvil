package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Util;

public class LambdaType extends Type implements ITyped, ITypeList
{
	public IType		returnType;
	public List<IType>	argumentTypes;
	
	public LambdaType()
	{
		this.argumentTypes = new ArrayList(2);
	}
	
	public LambdaType(TupleType tupleType)
	{
		this.argumentTypes = tupleType.types;
	}
	
	@Override
	public void setType(IType type)
	{
		this.returnType = type;
	}
	
	@Override
	public IType getType()
	{
		return this.returnType;
	}
	
	@Override
	public void setTypes(List<IType> types)
	{
		this.argumentTypes = types;
	}
	
	@Override
	public List<IType> getTypes()
	{
		return this.argumentTypes;
	}
	
	@Override
	public void addType(IType type)
	{
		this.argumentTypes.add(type);
	}
	
	@Override
	public Type resolve(IContext context)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(this.argumentTypes, buffer, true);
		buffer.append(Formatting.Expression.lambdaSeperator);
		this.returnType.toString("", buffer);
	}
}

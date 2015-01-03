package dyvil.tools.compiler.ast.type;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.api.ITypeList;
import dyvil.tools.compiler.util.Util;

public class TupleType extends Type implements ITypeList
{
	public List<IType>	types	= new ArrayList(2);
	
	@Override
	public void setTypes(List<IType> types)
	{
		this.types = types;
	}
	
	@Override
	public List<IType> getTypes()
	{
		return this.types;
	}
	
	@Override
	public void addType(IType type)
	{
		this.types.add(type);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		Util.parametersToString(this.types, buffer, true);
	}
}

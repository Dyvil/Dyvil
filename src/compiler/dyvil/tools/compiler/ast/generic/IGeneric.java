package dyvil.tools.compiler.ast.generic;

import java.util.List;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;

public interface IGeneric extends ITypeList
{
	public void setGeneric();
	
	public boolean isGeneric();
	
	@Override
	public default void setTypes(List<IType> types)
	{
	}
	
	@Override
	public default List<IType> getTypes()
	{
		return null;
	}
}

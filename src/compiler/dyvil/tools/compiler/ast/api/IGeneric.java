package dyvil.tools.compiler.ast.api;

import java.util.List;

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

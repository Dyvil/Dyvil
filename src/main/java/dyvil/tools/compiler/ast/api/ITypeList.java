package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.type.IType;

public interface ITypeList
{
	public void setTypes(List<IType> types);
	
	public List<IType> getTypes();
	
	public default void addType(IType type)
	{
		this.getTypes().add(type);
	}
}

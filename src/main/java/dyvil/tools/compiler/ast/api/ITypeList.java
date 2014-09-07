package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.type.Type;

public interface ITypeList
{
	public void setTypes(List<Type> types);
	
	public List<Type> getTypes();
	
	public default boolean addType(Type type)
	{
		return this.getTypes().add(type);
	}
}

package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.type.Type;

public interface ITypeList
{
	public void setTypes(List<Type> types);
	
	public List<Type> getTypes();
	
	public default void addType(Type type)
	{
		this.getTypes().add(type);
	}
}

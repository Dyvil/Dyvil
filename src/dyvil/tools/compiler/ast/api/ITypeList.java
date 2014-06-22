package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.type.Type;

public interface ITypeList
{
	public void addType(Type type);
	
	public List<Type> getTypes();
}

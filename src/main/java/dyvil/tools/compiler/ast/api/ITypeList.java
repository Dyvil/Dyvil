package dyvil.tools.compiler.ast.api;

import java.util.List;

public interface ITypeList
{
	public void setTypes(List<IType> types);
	
	public List<IType> getTypes();
	
	public default void addType(IType type)
	{
		this.getTypes().add(type);
	}
}

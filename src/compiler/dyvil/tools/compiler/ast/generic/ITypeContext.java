package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;

public interface ITypeContext
{
	public IType resolveType(ITypeVariable typeVar);
	
	public default void addMapping(ITypeVariable typeVar, IType type)
	{
	}
}

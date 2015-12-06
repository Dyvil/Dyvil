package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;

public interface ITypeContext
{
	ITypeContext DEFAULT = ITypeVariable::getDefaultType;

	IType resolveType(ITypeVariable typeVar);
	
	default void addMapping(ITypeVariable typeVar, IType type)
	{
	}
}

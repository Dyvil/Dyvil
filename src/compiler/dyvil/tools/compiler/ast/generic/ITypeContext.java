package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;

public interface ITypeContext
{
	ITypeContext NULL    = typeParameter -> null;
	ITypeContext DEFAULT = ITypeParameter::getDefaultType;

	IType resolveType(ITypeParameter typeParameter);
	
	default void addMapping(ITypeParameter typeVar, IType type)
	{
	}
}

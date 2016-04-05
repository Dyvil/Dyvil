package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;

public interface ITypeContext
{
	ITypeContext NULL    = typeParameter -> null;
	ITypeContext DEFAULT = ITypeParameter::getDefaultType;
	ITypeContext COVARIANT = ITypeParameter::getCovariantType;

	IType resolveType(ITypeParameter typeParameter);

	default boolean isReadonly()
	{
		return true;
	}
	
	default void addMapping(ITypeParameter typeVar, IType type)
	{
	}
}

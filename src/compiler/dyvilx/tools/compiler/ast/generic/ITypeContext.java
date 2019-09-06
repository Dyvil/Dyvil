package dyvilx.tools.compiler.ast.generic;

import dyvilx.tools.compiler.ast.type.IType;

public interface ITypeContext
{
	ITypeContext NULL      = typeParameter -> null;
	ITypeContext DEFAULT   = ITypeParameter::getUpperBound;
	ITypeContext COVARIANT = ITypeParameter::getCovariantType;

	static IType apply(ITypeContext typeContext, IType type)
	{
		if (typeContext != null)
		{
			return type.getConcreteType(typeContext);
		}
		return type;
	}

	IType resolveType(ITypeParameter typeParameter);

	default boolean isReadonly()
	{
		return true;
	}

	default boolean addMapping(ITypeParameter typeVar, IType type)
	{
		return false;
	}
}

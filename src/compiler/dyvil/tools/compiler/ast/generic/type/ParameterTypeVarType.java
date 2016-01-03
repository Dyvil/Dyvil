package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.type.IType;

public class ParameterTypeVarType extends TypeVarType
{
	public ParameterTypeVarType(ITypeParameter typeVariable)
	{
		super(typeVariable);
	}

	@Override
	public IType getParameterType()
	{
		return this;
	}

	@Override
	public boolean isSuperTypeOf(IType type)
	{
		return this.typeParameter.isAssignableFrom(type);
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		IType type = super.getConcreteType(context);
		if (type.getTypeVariable() == this.typeParameter)
		{
			return this;
		}
		return type;
	}
}

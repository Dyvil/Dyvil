package dyvil.tools.compiler.ast.generic.type;

import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;

public class ParameterTypeVarType extends TypeVarType
{
	public ParameterTypeVarType(ITypeVariable typeVariable)
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
		return this.typeVar.isAssignableFrom(type);
	}
}

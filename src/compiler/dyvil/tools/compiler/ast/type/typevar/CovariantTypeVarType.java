package dyvil.tools.compiler.ast.type.typevar;

import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.type.IType;

public class CovariantTypeVarType extends TypeVarType
{
	private final boolean backReference;

	public CovariantTypeVarType(ITypeParameter typeParameter)
	{
		super(typeParameter);
		this.backReference = false;
	}

	public CovariantTypeVarType(ITypeParameter typeParameter, boolean backReference)
	{
		super(typeParameter);
		this.backReference = backReference;
	}

	@Override
	public IType asParameterType()
	{
		return this;
	}

	@Override
	public boolean isUninferred()
	{
		return true;
	}

	@Override
	public int subTypeCheckLevel()
	{
		// Make sure CovariantTVT.isSubTypeOf(ResolvedTVT) is called instead of
		// ResolveTVT.isSuperTypeOf(CovariantTVT)
		return SUBTYPE_COVARIANT_TYPEVAR;
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.backReference || this.typeParameter.isSuperTypeOf(type);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return this.backReference || this.typeParameter.isAssignableFrom(subType, ITypeContext.COVARIANT);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return this.backReference || this.typeParameter.isSuperTypeOf(superType);
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType type = super.getConcreteType(context);

		final TypeVarType typeVar = type.extract(TypeVarType.class);
		return typeVar != null && typeVar.getTypeVariable() == this.typeParameter ? this : type;
	}
}

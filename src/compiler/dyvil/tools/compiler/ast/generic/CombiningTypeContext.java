package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;

public class CombiningTypeContext implements ITypeContext
{
	private ITypeContext context1;
	private ITypeContext context2;

	public CombiningTypeContext(ITypeContext context1, ITypeContext context2)
	{
		this.context1 = context1;
		this.context2 = context2;
	}

	@Override
	public boolean isReadonly()
	{
		return this.context1.isReadonly() && this.context2.isReadonly();
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		final IType type1 = this.context1.resolveType(typeParameter);
		final IType type2 = this.context2.resolveType(typeParameter);
		if (type1 == null)
		{
			return type2;
		}
		if (type2 == null)
		{
			return type1;
		}

		return Types.combine(type1, type2);
	}

	@Override
	public void addMapping(ITypeParameter typeVar, IType type)
	{
		this.context1.addMapping(typeVar, type);
		this.context2.addMapping(typeVar, type);
	}
}

package dyvilx.tools.compiler.ast.generic;

import dyvilx.tools.compiler.ast.type.IType;

import java.util.IdentityHashMap;
import java.util.Map;

public final class MapTypeContext implements ITypeContext
{
	private Map<ITypeParameter, IType> map = new IdentityHashMap<>();

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.map.get(typeParameter);
	}

	@Override
	public boolean isReadonly()
	{
		return false;
	}

	@Override
	public boolean addMapping(ITypeParameter typeVar, IType type)
	{
		this.map.put(typeVar, type);
		return true;
	}

	@Override
	public String toString()
	{
		return this.map.toString();
	}
}

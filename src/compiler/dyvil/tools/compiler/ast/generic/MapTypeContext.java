package dyvil.tools.compiler.ast.generic;

import dyvil.collection.Map;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.tools.compiler.ast.type.IType;

public final class MapTypeContext implements ITypeContext
{
	private Map<ITypeParameter, IType> map = new IdentityHashMap<>();
	
	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.map.get(typeParameter);
	}
	
	@Override
	public void addMapping(ITypeParameter typeVar, IType type)
	{
		this.map.put(typeVar, type);
	}
	
	@Override
	public String toString()
	{
		return this.map.toString();
	}
}

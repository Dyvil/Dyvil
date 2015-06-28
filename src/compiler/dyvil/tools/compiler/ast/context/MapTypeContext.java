package dyvil.tools.compiler.ast.context;

import dyvil.lang.Map;

import dyvil.collection.mutable.IdentityHashMap;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;

public final class MapTypeContext implements ITypeContext
{
	private Map<ITypeVariable, IType> map = new IdentityHashMap();
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		return this.map.get(typeVar);
	}
	
	@Override
	public void addMapping(ITypeVariable typeVar, IType type)
	{
		this.map.subscript_$eq(typeVar, type);
	}
	
	@Override
	public String toString()
	{
		return this.map.toString();
	}
}
package dyvil.tools.compiler.ast.context;

import dyvil.collection.Map;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;

public final class MapTypeContext implements ITypeContext
{
	private Map<ITypeVariable, IType> map = new IdentityHashMap();
	
	@Override
	public IType resolveType(ITypeVariable typeVar)
	{
		IType type = this.map.get(typeVar);
		if (type == null)
		{
			return Types.ANY;
		}
		return type;
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

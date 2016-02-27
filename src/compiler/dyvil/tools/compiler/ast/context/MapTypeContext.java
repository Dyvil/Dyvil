package dyvil.tools.compiler.ast.context;

import dyvil.collection.Map;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;

public final class MapTypeContext implements ITypeContext
{
	private Map<ITypeParameter, IType> map = new IdentityHashMap();
	
	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		IType type = this.map.get(typeParameter);
		if (type == null)
		{
			return Types.ANY;
		}
		return type;
	}
	
	@Override
	public void addMapping(ITypeParameter typeVar, IType type)
	{
		this.map.subscript_$eq(typeVar, type);
	}
	
	@Override
	public String toString()
	{
		return this.map.toString();
	}
}

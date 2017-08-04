package dyvil.tools.compiler.ast.type.typevar;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.lang.Name;
import dyvil.tools.parsing.marker.MarkerList;

public class InternalTypeVarType extends InternalType
{
	public InternalTypeVarType(String internalName)
	{
		super(internalName);
	}
	
	@Override
	public int typeTag()
	{
		return INTERNAL_TYPE_VAR;
	}
	
	@Override
	public Name getName()
	{
		return Name.fromRaw(this.internalName);
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		ITypeParameter typeVar = context.resolveTypeParameter(Name.fromRaw(this.internalName));
		if (typeVar == null)
		{
			return this;
		}
		
		return new TypeVarType(typeVar);
	}
}

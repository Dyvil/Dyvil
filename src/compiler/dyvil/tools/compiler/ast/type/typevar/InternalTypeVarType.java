package dyvil.tools.compiler.ast.type.typevar;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.raw.InternalType;
import dyvil.tools.parsing.Name;
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
		return Name.getQualified(this.internalName);
	}
	
	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		ITypeParameter typeVar = context.resolveTypeVariable(Name.getQualified(this.internalName));
		if (typeVar == null)
		{
			return this;
		}
		
		return new TypeVarType(typeVar);
	}
	
	@Override
	public String getInternalName()
	{
		return "java/lang/Object";
	}
	
	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ljava/lang/Object;");
	}
	
	@Override
	public void appendSignature(StringBuilder buffer)
	{
		buffer.append('T').append(this.internalName).append(';');
	}
}

package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.tools.compiler.ast.api.IClass;
import dyvil.tools.compiler.ast.api.IType;
import dyvil.tools.compiler.parser.type.ITypeVariable;

public class CaptureClass extends CodeClass
{
	public static int		captureID;
	
	public ITypeVariable	typeVariable;
	
	public CaptureClass(ITypeVariable typeVar, IType superType, List<IType> interfaces)
	{
		String name = "Capture$" + captureID++;
		this.typeVariable = typeVar;
		this.name = name;
		this.qualifiedName = name;
		this.internalName = superType.getInternalName();
		this.superType = superType;
		
		if (interfaces != null)
		{
			this.interfaces = interfaces;
		}
	}
	
	@Override
	public String getSignature()
	{
		return this.typeVariable.getSignature();
	}
	
	@Override
	public boolean equals(IClass iclass)
	{
		if (!iclass.isSuperType(this.superType))
		{
			return false;
		}
		
		for (IType i : this.interfaces)
		{
			if (!iclass.isSuperType(i))
			{
				return false;
			}
		}
		return true;
	}
}

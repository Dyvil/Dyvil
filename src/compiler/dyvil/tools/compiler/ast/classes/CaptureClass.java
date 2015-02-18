package dyvil.tools.compiler.ast.classes;

import java.util.List;

import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;

public class CaptureClass extends CodeClass
{
	public static int	captureID;
	
	public ITypeVariable var;
	public IType lowerBound;
	
	public CaptureClass(ITypeVariable var, IType superType, List<IType> interfaces, IType lowerBound)
	{
		String name = "Capture$" + captureID++;
		this.var = var;
		this.name = name;
		this.qualifiedName = name;
		this.superType = superType;
		
		if (interfaces != null)
		{
			this.interfaces = interfaces;
		}
	}
	
	public IType getLowerBound()
	{
		return this.lowerBound;
	}
}

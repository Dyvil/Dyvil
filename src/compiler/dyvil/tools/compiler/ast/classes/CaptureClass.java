package dyvil.tools.compiler.ast.classes;

import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.type.IType;

public class CaptureClass extends CodeClass
{
	private static int		captureID;
	
	public ITypeVariable	var;
	public IType[]			upperBounds;
	public int				upperBoundCount;
	public IType			lowerBound;
	
	public CaptureClass(ITypeVariable var, IType[] upperBounds, int upperBoundCount, IType lowerBound)
	{
		this.var = var;
	}
	
	public IType getLowerBound()
	{
		return this.lowerBound;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.name == null)
		{
			this.name = "capture-#" + captureID++;
		}
		
		buffer.append(this.name);
	}
}

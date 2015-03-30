package dyvil.tools.compiler.ast.generic;

import dyvil.tools.compiler.ast.type.IType;

public interface IBounded
{
	// Upper Bounds
	
	public int upperBoundCount();
	
	public void setUpperBound(int index, IType bound);
	
	public void addUpperBound(IType bound);
	
	public IType getUpperBound(int index);
	
	public IType[] getUpperBounds();
	
	// Lower Bounds
	
	public void setLowerBound(IType bound);
	
	public IType getLowerBound();
}

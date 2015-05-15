package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.type.IType;

public interface IVariable extends IField
{
	@Override
	public default boolean isVariable()
	{
		return true;
	}
	
	public void setIndex(int index);
	
	public int getIndex();
	
	public default IType getCaptureType(boolean init)
	{
		return null;
	}
}

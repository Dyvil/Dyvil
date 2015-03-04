package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.ITyped;

public interface IParameterized extends ITyped, ITypeList
{
	public default void setVarargs()
	{
	}
	
	public default boolean isVarargs()
	{
		return false;
	}
	
	public int parameterCount();
	
	public void setParameter(int index, Parameter param);
	
	public void addParameter(Parameter param);
	
	public Parameter getParameter(int index);
	
	@Override
	public default void addType(IType type)
	{
		int index = this.parameterCount();
		this.addParameter(new Parameter(index, "par" + index, type));
	}
}

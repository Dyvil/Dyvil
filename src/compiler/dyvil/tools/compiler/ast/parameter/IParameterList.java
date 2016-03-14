package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.consumer.IParameterConsumer;

public interface IParameterList extends IParameterConsumer
{
	int parameterCount();
	
	void setParameter(int index, IParameter parameter);
	
	@Override
	void addParameter(IParameter parameter);
	
	IParameter getParameter(int index);
	
	IParameter[] getParameters();
	
	default void setVariadic()
	{
	}
	
	default boolean isVariadic()
	{
		return false;
	}
}

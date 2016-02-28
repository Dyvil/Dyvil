package dyvil.tools.compiler.ast.parameter;

public interface IParameterList
{
	int parameterCount();
	
	void setParameter(int index, IParameter parameter);
	
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

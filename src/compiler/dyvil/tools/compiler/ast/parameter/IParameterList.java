package dyvil.tools.compiler.ast.parameter;

public interface IParameterList
{
	int parameterCount();
	
	void setParameter(int index, IParameter param);
	
	void addParameter(IParameter param);
	
	IParameter getParameter(int index);
	
	IParameter[] getParameters();
	
	default void setVarargs()
	{
	}
	
	default boolean isVarargs()
	{
		return false;
	}
}

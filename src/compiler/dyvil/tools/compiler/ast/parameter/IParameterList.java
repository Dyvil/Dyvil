package dyvil.tools.compiler.ast.parameter;


public interface IParameterList
{
	public int parameterCount();
	
	public void setParameter(int index, Parameter param);
	
	public void addParameter(Parameter param);
	
	public Parameter getParameter(int index);
	
	public default void setVarargs()
	{
	}
	
	public default boolean isVarargs()
	{
		return false;
	}
}

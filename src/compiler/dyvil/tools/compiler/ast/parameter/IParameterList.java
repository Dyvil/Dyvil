package dyvil.tools.compiler.ast.parameter;

public interface IParameterList
{
	public int parameterCount();
	
	public void setParameter(int index, IParameter param);
	
	public void addParameter(IParameter param);
	
	public IParameter getParameter(int index);
	
	public IParameter[] getParameters();
	
	public default void setVarargs()
	{
	}
	
	public default boolean isVarargs()
	{
		return false;
	}
}

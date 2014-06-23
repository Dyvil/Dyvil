package dyvil.tools.compiler.ast.api;

import java.util.Map;

import dyvil.tools.compiler.ast.method.Parameter;

public interface IParameterized extends ITyped
{
	public void setParameters(Map<String, Parameter> parameters);
	
	public Map<String, Parameter> getParameters();
	
	public default void addParameter(Parameter parameter)
	{
		this.getParameters().put(parameter.getName(), parameter);
	}
}

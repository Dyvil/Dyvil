package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.field.Parameter;

public interface IParameterized extends ITyped
{
	public void setVarargs();
	
	public boolean isVarargs();
	
	public void setParameters(List<Parameter> parameters);
	
	public List<Parameter> getParameters();
	
	public default void addParameter(Parameter parameter)
	{
		List<Parameter> parameters = this.getParameters();
		parameter.index = parameters.size();
		parameters.add(parameter);
	}
	
	public default void addParameterType(IType type)
	{
		int index = this.getParameters().size();
		this.addParameter(new Parameter(index, "par" + index, type));
	}
}

package dyvil.tools.compiler.ast.method;

import java.util.List;

import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;

public interface IParameterized extends ITyped
{
	public default void setVarargs()
	{
	}
	
	public default boolean isVarargs()
	{
		return false;
	}
	
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

package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.method.Parameter;
import dyvil.tools.compiler.ast.type.Type;

public interface IParameterized extends ITyped
{
	public void setParameters(List<Parameter> parameters);
	
	public List<Parameter> getParameters();
	
	public default void addParameter(Parameter parameter)
	{
		this.getParameters().add(parameter);
	}
	
	public default Type[] getParameterTypes()
	{
		List<Parameter> parameters = this.getParameters();
		Type[] types = new Type[parameters.size()];
		for (int i = 0; i < types.length; i++)
		{
			types[i] = parameters.get(i).getType();
		}
		return types;
	}
}

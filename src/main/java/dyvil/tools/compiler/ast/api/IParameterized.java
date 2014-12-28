package dyvil.tools.compiler.ast.api;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.field.Parameter;
import dyvil.tools.compiler.ast.type.Type;

public interface IParameterized extends ITyped, ITypeList
{
	public void setParameters(List<Parameter> parameters);
	
	public List<Parameter> getParameters();
	
	public default void addParameter(Parameter parameter)
	{
		List<Parameter> parameters = this.getParameters();
		parameter.index = parameters.size();
		parameters.add(parameter);
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
	
	@Override
	public default List<Type> getTypes()
	{
		List<Parameter> parameters = this.getParameters();
		List<Type> types = new ArrayList(parameters.size());
		for (Parameter param : parameters)
		{
			types.add(param.getType());
		}
		return types;
	}
	
	@Override
	public default void setTypes(List<Type> types)
	{
		int index = 0;
		for (Type type : types)
		{
			this.addParameter(new Parameter(index, "par" + index, type));
			index++;
		}
	}
	
	@Override
	public default void addType(Type type)
	{
		int index = this.getParameters().size();
		this.addParameter(new Parameter(index, "par" + index, type));
	}
}

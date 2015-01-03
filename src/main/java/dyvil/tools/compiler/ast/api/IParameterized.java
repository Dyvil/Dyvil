package dyvil.tools.compiler.ast.api;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.ast.field.Parameter;

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
	
	public default IType[] getParameterTypes()
	{
		List<Parameter> parameters = this.getParameters();
		IType[] types = new IType[parameters.size()];
		for (int i = 0; i < types.length; i++)
		{
			types[i] = parameters.get(i).getType();
		}
		return types;
	}
	
	@Override
	public default List<IType> getTypes()
	{
		List<Parameter> parameters = this.getParameters();
		List<IType> types = new ArrayList(parameters.size());
		for (Parameter param : parameters)
		{
			types.add(param.getType());
		}
		return types;
	}
	
	@Override
	public default void setTypes(List<IType> types)
	{
		int index = 0;
		for (IType type : types)
		{
			this.addParameter(new Parameter(index, "par" + index, type));
			index++;
		}
	}
	
	@Override
	public default void addType(IType type)
	{
		int index = this.getParameters().size();
		this.addParameter(new Parameter(index, "par" + index, type));
	}
}

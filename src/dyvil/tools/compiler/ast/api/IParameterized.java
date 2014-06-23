package dyvil.tools.compiler.ast.api;

import java.util.Map;

import dyvil.tools.compiler.ast.method.Parameter;

public interface IParameterized extends ITyped
{
	public boolean addParameter(Parameter parameter);
	
	public Map<String, Parameter> getParameters();
}

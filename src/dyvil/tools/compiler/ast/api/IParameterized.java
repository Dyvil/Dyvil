package dyvil.tools.compiler.ast.api;

import java.util.List;

import dyvil.tools.compiler.ast.method.Parameter;

public interface IParameterized extends ITyped
{
	public boolean addParameter(Parameter parameter);
	
	public List<Parameter> getParameters();
}

package dyvil.tools.compiler.ast.annotation;

import java.util.HashMap;
import java.util.Map;

import clashsoft.cslib.src.SyntaxException;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.type.Type;

public class Annotation implements ITyped
{
	private Type type;
	
	private Map<String, Variable> parameters = new HashMap();
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	public void addParameter(Variable var) throws SyntaxException
	{
		String key = var.getName();
		this.parameters.put(key, var);
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	public Map<String, Variable> getParameters()
	{
		return this.parameters;
	}
}

package dyvil.tools.compiler.ast.annotation;

import java.util.HashMap;
import java.util.Map;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.field.Field;
import dyvil.tools.compiler.ast.type.Type;

public class Annotation extends ASTObject implements ITyped
{
	private Type type;
	
	private Map<String, Field> parameters = new HashMap();
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	public void addParameter(Field var)
	{
		this.parameters.put(var.getName(), var);
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	public Map<String, Field> getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public Annotation applyState(CompilerState state)
	{
		return this;
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		// TODO
	}
}

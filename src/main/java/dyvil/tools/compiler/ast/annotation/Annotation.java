package dyvil.tools.compiler.ast.annotation;

import java.util.ArrayList;
import java.util.List;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.ASTObject;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.api.IValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class Annotation extends ASTObject implements ITyped, IValueList
{
	public Type			type;
	public List<IValue>	parameters	= new ArrayList();
	
	private String		name;
	
	public Annotation(ICodePosition position, String name)
	{
		this.position = position;
		this.name = name;
	}
	
	@Override
	public void setType(Type type)
	{
		this.type = type;
	}
	
	@Override
	public Type getType()
	{
		return this.type;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
		this.parameters = list;
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.parameters;
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.parameters.add(value);
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.parameters.set(index, value);
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.parameters.get(index);
	}
	
	@Override
	public void setArray(boolean array)
	{}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public Annotation applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		Util.parametersToString(this.parameters, buffer, false);
	}
}

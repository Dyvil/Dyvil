package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.api.ITyped;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.type.Type;

public class Variable extends Member implements IField, ITyped
{
	private Object	value;
	
	public Variable()
	{
	}
	
	public Variable(String name)
	{
		super(name);
	}
	
	public Variable(String name, Type type)
	{
		super(name, type);
	}
	
	public Variable(String name, Type type, int modifiers)
	{
		super(name, type, modifiers);
	}
	
	@Override
	public void setValue(Object value)
	{
		this.value = value;
	}
	
	@Override
	public Object getValue()
	{
		return this.value;
	}
}

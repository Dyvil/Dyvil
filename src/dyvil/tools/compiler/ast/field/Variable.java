package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class Variable extends Member implements IField
{
	private IValue	value;
	
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
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
}

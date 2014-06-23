package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.type.Type;

public class Variable extends Member implements IField
{
	private Object	value;
	
	public Variable()
	{
	}
	
	public Variable(String name, Type type, int modifiers, Object value)
	{
		super(name, type, modifiers);
		this.value = value;
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

package dyvil.tools.compiler.ast.field;

import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.method.Member;
import dyvil.tools.compiler.ast.type.Type;

public class Variable extends Member implements IField
{
	private Value	value;
	
	public Variable()
	{
	}
	
	public Variable(String name, Type type, int modifiers, Value value)
	{
		super(name, type, modifiers);
		this.setValue(value);
	}
	
	@Override
	public void setValue(Value value)
	{
		this.value = value;
	}
	
	@Override
	public Value getValue()
	{
		return this.value;
	}
}

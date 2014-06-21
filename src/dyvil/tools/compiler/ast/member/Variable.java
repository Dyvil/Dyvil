package dyvil.tools.compiler.ast.member;

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
	
	public Value getValue()
	{
		return this.value;
	}
}

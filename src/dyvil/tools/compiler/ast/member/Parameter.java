package dyvil.tools.compiler.ast.member;

public class Parameter extends Member
{
	public Parameter()
	{
	}
	
	public Parameter(String name, Type type, int modifiers)
	{
		super(name, type, modifiers);
	}
}

package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.ast.type.Type;


public class Parameter extends Member
{
	private char seperator;
	
	public Parameter()
	{
	}
	
	public Parameter(String name, Type type, int modifiers)
	{
		this(name, type, modifiers, ',');
	}
	
	public Parameter(String name, Type type, int modifiers, char seperator)
	{
		super(name, type, modifiers);
		this.seperator = seperator;
	}
	
	public void setSeperator(char seperator)
	{
		this.seperator = seperator;
	}
	
	public char getSeperator()
	{
		return this.seperator;
	}
}

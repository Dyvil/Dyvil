package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;

public class Parameter extends Member
{
	private char	seperator;
	
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

	@Override
	public Parameter applyState(CompilerState state, IContext context)
	{
		return this;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
		buffer.append(' ').append(this.name);
	}
}

package dyvil.tools.compiler.ast.method;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.api.IField;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;

public class Parameter extends Member implements IField
{
	private char	seperator;
	
	public Parameter()
	{
		super(null);
	}
	
	public Parameter(String name, Type type, int modifiers)
	{
		this(name, type, modifiers, ',');
	}
	
	public Parameter(String name, Type type, int modifiers, char seperator)
	{
		super(null, name, type, modifiers);
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

	@Override
	public void setValue(IValue value)
	{}

	@Override
	public IValue getValue()
	{
		return null;
	}
}

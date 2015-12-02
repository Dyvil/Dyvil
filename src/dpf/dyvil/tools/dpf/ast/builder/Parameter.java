package dyvil.tools.dpf.ast.builder;

import dyvil.tools.dpf.ast.value.Value;
import dyvil.tools.dpf.ast.value.ValueCreator;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class Parameter extends ValueCreator implements IASTNode
{
	private Name  name;
	private Value value;
	
	public Parameter(Name name)
	{
		this.name = name;
	}
	
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	protected void setValue(Value value)
	{
		this.value = value;
	}
	
	public Value getValue()
	{
		return this.value;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.name != null)
		{
			buffer.append(this.name).append(": ");
		}
		this.value.toString(prefix, buffer);
	}
}

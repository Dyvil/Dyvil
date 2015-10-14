package dyvil.tools.dpf.ast;

import dyvil.tools.dpf.ast.value.Value;
import dyvil.tools.dpf.ast.value.ValueCreator;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.position.ICodePosition;

public class Property extends ValueCreator implements NodeElement
{
	protected Name name;
	protected Value value;
	
	private ICodePosition position;
	
	public Property(Name name)
	{
		this.name = name;
	}
	
	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	public Value getValue()
	{
		return this.value;
	}
	
	@Override
	public void setValue(Value value)
	{
		this.value = value;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(" = ").append(this.value);
	}
}

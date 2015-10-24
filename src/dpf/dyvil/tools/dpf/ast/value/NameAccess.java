package dyvil.tools.dpf.ast.value;

import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;

public class NameAccess extends ValueCreator implements Value
{
	public Value value;
	public Name name;
	
	public NameAccess(Name name)
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
	public void accept(ValueVisitor visitor)
	{
		this.value.accept(visitor.visitValueAccess(this.name));
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.value.toString(prefix, buffer);
		buffer.append('.').append(this.name);
	}
}

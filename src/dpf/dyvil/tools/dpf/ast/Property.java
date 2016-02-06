package dyvil.tools.dpf.ast;

import dyvil.collection.Map;
import dyvil.tools.dpf.ast.value.Value;
import dyvil.tools.dpf.converter.DPFValueVisitor;
import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class Property extends DPFValueVisitor implements NodeElement, Expandable
{
	protected Name  name;
	protected Value value;
	
	private ICodePosition position;
	
	public Property(Name name)
	{
		this.name = name;
	}

	public Property(Name name, ICodePosition position)
	{
		this.name = name;
		this.position = position;
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
	
	public Value getValue()
	{
		return this.value;
	}
	
	@Override
	protected void setValue(Value value)
	{
		this.value = value;
	}
	
	@Override
	public void accept(NodeVisitor visitor)
	{
		this.value.accept(visitor.visitProperty(this.name));
	}

	@Override
	public Property expand(Map<String, Object> mappings, boolean mutate)
	{
		Property property = mutate ? this : new Property(this.name, this.position);
		property.value = Value.wrap(Expandable.expand(this.value, mappings, mutate));
		return property;
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append(" = ");
		this.value.toString(prefix, buffer);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !(obj instanceof Property))
		{
			return false;
		}

		final Property other = (Property) obj;
		return this.name == other.name // equal name
				&& this.value.equals(other.value); // equal value
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = this.name.hashCode();
		result = prime * result + this.value.hashCode();
		return result;
	}
}

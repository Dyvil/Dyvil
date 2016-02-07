package dyvil.tools.dpf.ast;

import dyvil.collection.Map;
import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class NodeAccess implements NodeVisitor, NodeElement, Expandable
{
	protected Name        name;
	protected NodeElement element;
	
	private ICodePosition position;
	
	public NodeAccess(Name name)
	{
		this.name = name;
	}

	public NodeAccess(Name name, ICodePosition position)
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
	
	@Override
	public NodeVisitor visitNode(Name name)
	{
		Node node = new Node(name);
		this.element = node;
		return node;
	}
	
	@Override
	public ValueVisitor visitProperty(Name name)
	{
		Property property = new Property(name);
		this.element = property;
		return property;
	}
	
	@Override
	public NodeVisitor visitNodeAccess(Name name)
	{
		NodeAccess access = new NodeAccess(name);
		this.element = access;
		return access;
	}
	
	@Override
	public void accept(NodeVisitor visitor)
	{
		this.element.accept(visitor.visitNodeAccess(this.name));
	}

	@Override
	public NodeAccess expand(Map<String, Object> mappings, boolean mutate)
	{
		NodeAccess nodeAccess = mutate ? this : new NodeAccess(this.name, this.position);
		nodeAccess.element = (NodeElement) Expandable.expand(this.element, mappings, mutate);
		return nodeAccess;
	}

	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name).append('.');
		this.element.toString(prefix, buffer);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || !(obj instanceof NodeAccess))
		{
			return false;
		}

		final NodeAccess other = (NodeAccess) obj;
		return this.name == other.name // equal name
				&& this.element.equals(other.element); // equal element
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = this.name.hashCode();
		result = prime * result + this.element.hashCode();
		return result;
	}
}

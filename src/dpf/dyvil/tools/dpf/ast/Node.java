package dyvil.tools.dpf.ast;

import dyvil.collection.List;
import dyvil.collection.mutable.ArrayList;
import dyvil.tools.dpf.visitor.NodeVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public class Node implements NodeElement, NodeVisitor
{
	protected Name				name;
	protected List<Node>		nodes		= new ArrayList<Node>();
	protected List<Property>	properties	= new ArrayList<Property>();
	
	protected ICodePosition position;
	
	public Node(Name name)
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
	
	@Override
	public NodeVisitor visitNode(Name name)
	{
		Node node = new Node(name);
		this.nodes.add(node);
		return node;
	}
	
	@Override
	public ValueVisitor visitProperty(Name name)
	{
		Property property = new Property(name);
		this.properties.add(property);
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
		buffer.append(this.name).append('\n').append(prefix).append('{').append('\n');
		
		String s = prefix + "\t";
		for (Property property : this.properties)
		{
			buffer.append(s);
			property.toString(s, buffer);
			buffer.append('\n');
		}
		
		for (Node node : this.nodes)
		{
			buffer.append(s).append('\n').append(s);
			node.toString(s, buffer);
			buffer.append('\n');
		}
		
		buffer.append(prefix).append('}');
	}
}

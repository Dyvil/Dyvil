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
	protected Name name;
	protected List<Node>       nodes        = new ArrayList<>();
	protected List<Property>   properties   = new ArrayList<>();
	protected List<NodeAccess> nodeAccesses = new ArrayList<>();
	
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
	public NodeVisitor visitNodeAccess(Name name)
	{
		NodeAccess access = new NodeAccess(name);
		this.nodeAccesses.add(access);
		return access;
	}
	
	@Override
	public void accept(NodeVisitor visitor)
	{
		NodeVisitor nodeVisitor = visitor.visitNode(this.name);
		for (Property element : this.properties)
		{
			element.accept(nodeVisitor);
		}
		for (Node node : this.nodes)
		{
			node.accept(nodeVisitor);
		}
		for (NodeAccess nodeAccess : this.nodeAccesses)
		{
			nodeAccess.accept(nodeVisitor);
		}
		nodeVisitor.visitEnd();
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
		
		this.bodyToString(prefix + "\t", buffer);
		
		buffer.append(prefix).append('}');
	}
	
	public void bodyToString(String prefix, StringBuilder buffer)
	{
		for (NodeElement element : this.properties)
		{
			buffer.append(prefix);
			element.toString(prefix, buffer);
			buffer.append('\n');
		}
		
		for (Node node : this.nodes)
		{
			buffer.append(prefix).append('\n').append(prefix);
			node.toString(prefix, buffer);
			buffer.append('\n');
		}

		for (NodeAccess nodeAccess : this.nodeAccesses)
		{
			buffer.append(prefix).append('\n').append(prefix);
			nodeAccess.toString(prefix, buffer);
			buffer.append('\n');
		}
	}
}
